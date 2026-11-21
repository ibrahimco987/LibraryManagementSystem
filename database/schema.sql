BEGIN;

CREATE TABLE public.books (
    id INTEGER PRIMARY KEY CHECK (id > 0),
    title TEXT NOT NULL CHECK (length(trim(title)) > 0),
    author TEXT NOT NULL CHECK (length(trim(author)) > 0),
    status TEXT NOT NULL DEFAULT 'Available'
        CHECK (status IN ('Available', 'Issued', 'Reserved')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE public.librarians (
    id UUID PRIMARY KEY REFERENCES auth.users(id)
        ON DELETE CASCADE,
    name TEXT NOT NULL CHECK (length(trim(name)) > 0)
);

CREATE TABLE public.activity_logs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    librarian_id UUID NOT NULL REFERENCES public.librarians(id),
    book_id INTEGER,
    action TEXT NOT NULL
        CHECK (action IN ('ADD', 'UPDATE', 'DELETE', 'STATUS_CHANGE')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE public.books ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.librarians ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.activity_logs ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.books, public.librarians,
    public.activity_logs FROM anon, authenticated;

COMMIT;