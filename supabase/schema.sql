-- MeuPersonal / Supabase
-- Execute no SQL Editor do novo projeto.
-- A chave service_role/secret nunca deve ser usada no APK.

create extension if not exists pgcrypto;

create table if not exists public.profiles (
  user_id uuid primary key references auth.users(id) on delete cascade,
  full_name text,
  age integer,
  weight_kg numeric,
  height_cm numeric,
  gender text,
  activity_level text,
  experience_level text,
  updated_at timestamptz not null default now()
);

create table if not exists public.workout_plans (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  client_id uuid not null,
  title text not null,
  objective text not null default '',
  plan_json jsonb not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (user_id, client_id)
);

create table if not exists public.workout_sessions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  client_plan_id uuid,
  title text not null,
  started_at timestamptz not null,
  finished_at timestamptz,
  completed boolean not null default false,
  created_at timestamptz not null default now()
);

alter table public.profiles enable row level security;
alter table public.workout_plans enable row level security;
alter table public.workout_sessions enable row level security;

drop policy if exists profiles_owner on public.profiles;
create policy profiles_owner on public.profiles
  for all to authenticated
  using (user_id = auth.uid())
  with check (user_id = auth.uid());

drop policy if exists workout_plans_owner on public.workout_plans;
create policy workout_plans_owner on public.workout_plans
  for all to authenticated
  using (user_id = auth.uid())
  with check (user_id = auth.uid());

drop policy if exists workout_sessions_owner on public.workout_sessions;
create policy workout_sessions_owner on public.workout_sessions
  for all to authenticated
  using (user_id = auth.uid())
  with check (user_id = auth.uid());

create or replace function public.set_updated_at()
returns trigger language plpgsql as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

drop trigger if exists profiles_set_updated_at on public.profiles;
create trigger profiles_set_updated_at before update on public.profiles
for each row execute function public.set_updated_at();

drop trigger if exists workout_plans_set_updated_at on public.workout_plans;
create trigger workout_plans_set_updated_at before update on public.workout_plans
for each row execute function public.set_updated_at();
