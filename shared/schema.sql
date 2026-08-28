-- ============================================================================
-- HOSTEL MANAGEMENT — SUPABASE SCHEMA
-- Run this in Supabase → SQL Editor (or via `supabase db push` after saving
-- it as a migration). Safe to run once on a fresh project. If a table
-- already exists from earlier experiments, drop it first or adjust names.
-- ============================================================================

-- Extension needed for gen_random_uuid()
create extension if not exists "pgcrypto";

-- ============================================================================
-- 1. ROOMS
-- ============================================================================
create table if not exists rooms (
  id uuid primary key default gen_random_uuid(),
  room_number text unique not null,       -- e.g. "A-304"
  block text,                              -- e.g. "North Block"
  capacity int not null default 2,
  created_at timestamptz default now()
);

-- ============================================================================
-- 2. PROFILES (extends Supabase auth.users)
-- ============================================================================
create table if not exists profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  full_name text,
  email text,
  phone text,
  role text not null default 'student' check (role in ('student','admin','warden')),
  room_id uuid references rooms(id) on delete set null,
  avatar_url text,
  created_at timestamptz default now()
);

-- auto-create a profile row whenever someone signs up via Supabase Auth
create or replace function public.handle_new_user()
returns trigger as $$
begin
  insert into public.profiles (id, email, full_name)
  values (new.id, new.email, new.raw_user_meta_data->>'full_name');
  return new;
end;
$$ language plpgsql security definer;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute procedure public.handle_new_user();

-- ============================================================================
-- 3. MESS MENU + MESS BALANCE / TRANSACTIONS
-- ============================================================================
create table if not exists mess_menu (
  id uuid primary key default gen_random_uuid(),
  day date not null,
  meal text not null check (meal in ('Breakfast','Lunch','Snacks','Dinner')),
  items text,
  calories int,
  serve_time time,
  unique (day, meal)
);

create table if not exists mess_balance (
  student_id uuid primary key references profiles(id) on delete cascade,
  balance numeric(10,2) not null default 0,
  updated_at timestamptz default now()
);

create table if not exists mess_transactions (
  id uuid primary key default gen_random_uuid(),
  student_id uuid references profiles(id) on delete cascade,
  amount numeric(10,2) not null,           -- positive = recharge, negative = deduction
  type text check (type in ('recharge','deduction','refund')),
  note text,
  created_at timestamptz default now()
);

-- ============================================================================
-- 4. COMPLAINTS
-- ============================================================================
create table if not exists complaints (
  id uuid primary key default gen_random_uuid(),
  student_id uuid references profiles(id) on delete cascade,
  title text not null,
  description text,
  category text check (category in ('Electrical','Plumbing','AC','Furniture','Internet','Cleanliness','Other')) default 'Other',
  status text not null default 'Submitted' check (status in ('Submitted','Assigned','In Progress','Resolved')),
  assigned_to uuid references profiles(id),  -- staff/admin handling it
  created_at timestamptz default now(),
  resolved_at timestamptz
);

-- ============================================================================
-- 5. NOTICES
-- ============================================================================
create table if not exists notices (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  body text,
  priority text not null default 'Low' check (priority in ('Low','Medium','High')),
  posted_by uuid references profiles(id),
  created_at timestamptz default now()
);

-- ============================================================================
-- 6. EVENTS + REGISTRATIONS
-- ============================================================================
create table if not exists events (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  description text,
  location text,
  event_date date not null,
  banner_url text,
  created_at timestamptz default now()
);

create table if not exists event_registrations (
  id uuid primary key default gen_random_uuid(),
  event_id uuid references events(id) on delete cascade,
  student_id uuid references profiles(id) on delete cascade,
  registered_at timestamptz default now(),
  unique (event_id, student_id)
);

-- ============================================================================
-- 7. LAUNDRY
-- ============================================================================
create table if not exists laundry_orders (
  id uuid primary key default gen_random_uuid(),
  student_id uuid references profiles(id) on delete cascade,
  items_count int not null default 1,
  status text not null default 'Dropped' check (status in ('Dropped','Washing','Drying','Ready','Collected')),
  dropped_at timestamptz default now(),
  ready_at timestamptz,
  collected_at timestamptz
);

-- ============================================================================
-- 8. PARCELS
-- ============================================================================
create table if not exists parcels (
  id uuid primary key default gen_random_uuid(),
  student_id uuid references profiles(id) on delete cascade,
  sender text,
  description text,
  status text not null default 'Arrived' check (status in ('Arrived','Ready for Pickup','Collected')),
  arrived_at timestamptz default now(),
  picked_up_at timestamptz
);

-- ============================================================================
-- 9. VISITORS
-- ============================================================================
create table if not exists visitors (
  id uuid primary key default gen_random_uuid(),
  student_id uuid references profiles(id) on delete cascade,
  visitor_name text not null,
  visitor_phone text,
  purpose text,
  status text not null default 'Pending' check (status in ('Pending','Approved','Rejected','Checked In','Checked Out')),
  requested_at timestamptz default now(),
  approved_at timestamptz,
  approved_by uuid references profiles(id)
);

-- ============================================================================
-- 10. FEES + PAYMENTS
-- ============================================================================
create table if not exists fees (
  id uuid primary key default gen_random_uuid(),
  student_id uuid references profiles(id) on delete cascade,
  title text not null,               -- e.g. "Hostel Fee - Semester 1"
  amount numeric(10,2) not null,
  due_date date,
  status text not null default 'Pending' check (status in ('Pending','Paid','Overdue')),
  created_at timestamptz default now()
);

create table if not exists payments (
  id uuid primary key default gen_random_uuid(),
  fee_id uuid references fees(id) on delete cascade,
  student_id uuid references profiles(id) on delete cascade,
  amount numeric(10,2) not null,
  method text check (method in ('UPI','Card','NetBanking','Cash')),
  transaction_ref text,
  paid_at timestamptz default now()
);

-- ============================================================================
-- 11. ATTENDANCE
-- ============================================================================
create table if not exists attendance (
  id uuid primary key default gen_random_uuid(),
  student_id uuid references profiles(id) on delete cascade,
  date date not null,
  status text not null check (status in ('Present','Absent','Leave')),
  marked_by uuid references profiles(id),
  unique (student_id, date)
);

-- ============================================================================
-- 12. EMERGENCY CONTACTS (static-ish reference data, admin-managed)
-- ============================================================================
create table if not exists emergency_contacts (
  id uuid primary key default gen_random_uuid(),
  label text not null,               -- e.g. "Warden"
  description text,                  -- e.g. "North Block"
  phone text not null,
  type text check (type in ('Warden','Medical','Security','Office'))
);

-- ============================================================================
-- INDEXES for the queries the dashboard actually runs
-- ============================================================================
create index if not exists idx_complaints_student on complaints(student_id, status);
create index if not exists idx_notices_created on notices(created_at desc);
create index if not exists idx_events_date on events(event_date);
create index if not exists idx_laundry_student on laundry_orders(student_id, status);
create index if not exists idx_parcels_student on parcels(student_id, status);
create index if not exists idx_visitors_student on visitors(student_id, status);
create index if not exists idx_fees_student on fees(student_id, status);
create index if not exists idx_attendance_student_date on attendance(student_id, date desc);

-- ============================================================================
-- ROW LEVEL SECURITY
-- ============================================================================

create or replace function public.is_staff()
returns boolean as $$
  select exists (
    select 1 from profiles
    where id = auth.uid() and role in ('admin','warden')
  );
$$ language sql security definer stable;

-- PROFILES
alter table profiles enable row level security;
drop policy if exists "read own profile" on profiles;
create policy "read own profile" on profiles for select using (auth.uid() = id or is_staff());
drop policy if exists "update own profile" on profiles;
create policy "update own profile" on profiles for update using (auth.uid() = id);

-- ROOMS
alter table rooms enable row level security;
drop policy if exists "everyone reads rooms" on rooms;
create policy "everyone reads rooms" on rooms for select using (true);
drop policy if exists "staff manage rooms" on rooms;
create policy "staff manage rooms" on rooms for all using (is_staff());

-- MESS MENU
alter table mess_menu enable row level security;
drop policy if exists "everyone reads mess menu" on mess_menu;
create policy "everyone reads mess menu" on mess_menu for select using (true);
drop policy if exists "staff manage mess menu" on mess_menu;
create policy "staff manage mess menu" on mess_menu for all using (is_staff());

-- MESS BALANCE / TRANSACTIONS
alter table mess_balance enable row level security;
drop policy if exists "own mess balance" on mess_balance;
create policy "own mess balance" on mess_balance for select using (auth.uid() = student_id or is_staff());
drop policy if exists "staff update mess balance" on mess_balance;
create policy "staff update mess balance" on mess_balance for all using (is_staff());

alter table mess_transactions enable row level security;
drop policy if exists "own mess transactions" on mess_transactions;
create policy "own mess transactions" on mess_transactions for select using (auth.uid() = student_id or is_staff());
drop policy if exists "staff manage mess transactions" on mess_transactions;
create policy "staff manage mess transactions" on mess_transactions for insert with check (is_staff());

-- COMPLAINTS
alter table complaints enable row level security;
drop policy if exists "own complaints select" on complaints;
create policy "own complaints select" on complaints for select using (auth.uid() = student_id or is_staff());
drop policy if exists "own complaints insert" on complaints;
create policy "own complaints insert" on complaints for insert with check (auth.uid() = student_id);
drop policy if exists "staff update complaints" on complaints;
create policy "staff update complaints" on complaints for update using (is_staff());

-- NOTICES
alter table notices enable row level security;
drop policy if exists "everyone reads notices" on notices;
create policy "everyone reads notices" on notices for select using (true);
drop policy if exists "staff post notices" on notices;
create policy "staff post notices" on notices for insert with check (is_staff());
drop policy if exists "staff manage notices" on notices;
create policy "staff manage notices" on notices for update using (is_staff());

-- EVENTS
alter table events enable row level security;
drop policy if exists "everyone reads events" on events;
create policy "everyone reads events" on events for select using (true);
drop policy if exists "staff manage events" on events;
create policy "staff manage events" on events for all using (is_staff());

alter table event_registrations enable row level security;
drop policy if exists "own registrations" on event_registrations;
create policy "own registrations" on event_registrations for select using (auth.uid() = student_id or is_staff());
drop policy if exists "register self" on event_registrations;
create policy "register self" on event_registrations for insert with check (auth.uid() = student_id);

-- LAUNDRY
alter table laundry_orders enable row level security;
drop policy if exists "own laundry" on laundry_orders;
create policy "own laundry" on laundry_orders for select using (auth.uid() = student_id or is_staff());
drop policy if exists "own laundry insert" on laundry_orders;
create policy "own laundry insert" on laundry_orders for insert with check (auth.uid() = student_id);
drop policy if exists "staff update laundry" on laundry_orders;
create policy "staff update laundry" on laundry_orders for update using (is_staff());

-- PARCELS
alter table parcels enable row level security;
drop policy if exists "own parcels" on parcels;
create policy "own parcels" on parcels for select using (auth.uid() = student_id or is_staff());
drop policy if exists "staff manage parcels" on parcels;
create policy "staff manage parcels" on parcels for all using (is_staff());

-- VISITORS
alter table visitors enable row level security;
drop policy if exists "own visitor requests select" on visitors;
create policy "own visitor requests select" on visitors for select using (auth.uid() = student_id or is_staff());
drop policy if exists "own visitor requests insert" on visitors;
create policy "own visitor requests insert" on visitors for insert with check (auth.uid() = student_id);
drop policy if exists "staff approve visitors" on visitors;
create policy "staff approve visitors" on visitors for update using (is_staff());

-- FEES / PAYMENTS
alter table fees enable row level security;
drop policy if exists "own fees" on fees;
create policy "own fees" on fees for select using (auth.uid() = student_id or is_staff());
drop policy if exists "staff manage fees" on fees;
create policy "staff manage fees" on fees for all using (is_staff());

alter table payments enable row level security;
drop policy if exists "own payments select" on payments;
create policy "own payments select" on payments for select using (auth.uid() = student_id or is_staff());
drop policy if exists "own payments insert" on payments;
create policy "own payments insert" on payments for insert with check (auth.uid() = student_id);

-- ATTENDANCE
alter table attendance enable row level security;
drop policy if exists "own attendance select" on attendance;
create policy "own attendance select" on attendance for select using (auth.uid() = student_id or is_staff());
drop policy if exists "staff mark attendance" on attendance;
create policy "staff mark attendance" on attendance for all using (is_staff());

-- EMERGENCY CONTACTS
alter table emergency_contacts enable row level security;
drop policy if exists "everyone reads emergency contacts" on emergency_contacts;
create policy "everyone reads emergency contacts" on emergency_contacts for select using (true);
drop policy if exists "staff manage emergency contacts" on emergency_contacts;
create policy "staff manage emergency contacts" on emergency_contacts for all using (is_staff());
