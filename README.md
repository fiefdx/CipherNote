# CipherNote

CipherNote is a secure notes manager for Android.

## Core Functional Features

- **Password‑protected notes**: Users provide a password; the app derives a key using MD5 and encrypts note content with the TEA algorithm.
- **Create, edit, delete notes**: Full CRUD operations on notes stored in an SQLite database.
- **Encrypted storage**: All note contents are saved encrypted; decryption occurs only after correct password entry.
- **Automatic timestamping**: Notes record creation and modification timestamps.
- **Notes list & sorting**: Notes are displayed in a list and can be sorted by modification date.
- **Secure note opening**: Wrong password triggers an error dialog; correct password reveals decrypted content.
- **UI flow**: Initial screen → notes list → edit screen, with smooth Compose animations.
