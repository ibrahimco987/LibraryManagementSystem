BEGIN;

GRANT SELECT ON public.librarians TO authenticated;

GRANT SELECT, INSERT, UPDATE, DELETE
ON public.books TO authenticated;

GRANT SELECT ON public.activity_logs TO authenticated;

CREATE POLICY "View own librarian profile"
ON public.librarians
FOR SELECT
TO authenticated
USING (id = auth.uid());

CREATE POLICY "Librarians can manage books"
ON public.books
FOR ALL
TO authenticated
USING (
    EXISTS (
        SELECT 1 FROM public.librarians
        WHERE id = auth.uid()
    )
)
WITH CHECK (
    EXISTS (
        SELECT 1 FROM public.librarians
        WHERE id = auth.uid()
    )
);

CREATE POLICY "Librarians can view activity logs"
ON public.activity_logs
FOR SELECT
TO authenticated
USING (
    EXISTS (
        SELECT 1 FROM public.librarians
        WHERE id = auth.uid()
    )
);

COMMIT;