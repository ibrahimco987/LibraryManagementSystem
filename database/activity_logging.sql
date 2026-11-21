BEGIN;

ALTER TABLE public.activity_logs
ADD COLUMN librarian_name TEXT;

CREATE OR REPLACE FUNCTION public.log_book_activity()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = ''
AS $$
DECLARE
    user_id UUID;
    user_name TEXT;
    action_type TEXT;
    affected_book_id INTEGER;
BEGIN

    user_id := auth.uid();

    IF user_id IS NULL THEN
        RETURN NULL;
    END IF;

    SELECT name INTO user_name
    FROM public.librarians
    WHERE id = user_id;

    IF TG_OP = 'INSERT' THEN

        action_type := 'ADD';
        affected_book_id := NEW.id;

    ELSIF TG_OP = 'UPDATE' THEN

        affected_book_id := NEW.id;

        IF OLD.status IS DISTINCT FROM NEW.status THEN
            action_type := 'STATUS_CHANGE';
        ELSE
            action_type := 'UPDATE';
        END IF;

    ELSIF TG_OP = 'DELETE' THEN

        action_type := 'DELETE';
        affected_book_id := OLD.id;

    END IF;

    INSERT INTO public.activity_logs
        (librarian_id, librarian_name, book_id, action)
    VALUES
        (user_id, user_name, affected_book_id, action_type);

    RETURN NULL;
END;
$$;

CREATE TRIGGER book_activity_trigger
AFTER INSERT OR UPDATE OR DELETE
ON public.books
FOR EACH ROW
EXECUTE FUNCTION public.log_book_activity();

REVOKE EXECUTE ON FUNCTION public.log_book_activity()
FROM PUBLIC, anon, authenticated;

COMMIT;