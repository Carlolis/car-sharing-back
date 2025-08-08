# Refactoring Suggestions for Car Sharing App Backend

## 1. Extract Configuration to a Dedicated Configuration Layer

- `SardineScalaImpl.scala`: Environment variables and hardcoded values should be moved to a configuration layer:
    - Lines 10-17: NEXTCLOUD_USERNAME, NEXTCLOUD_PASSWORD, NEXTCLOUD_URL
    - Line 18: Hardcoded path construction
- `AuthServiceLive.scala`:
    - Line 11: Hardcoded secret key "your-secret-key-here" should be moved to configuration
    - Line 51: Token expiration time (36000000 seconds) should be configurable

## 2. Eliminate Code Duplication

- `SardineScalaImpl.scala`: The two `put` methods (lines 34-48 and 50-61) share significant duplicate code
- Error handling patterns are duplicated across multiple files with similar tapError/tapBoth patterns

## 3. Implement Missing Methods

- `InvoiceServiceLive.scala`: Methods `getAllInvoices` and `deleteInvoice` are marked with `???` (lines 66-68)

## 4. Improve Error Handling

- `InvoiceWebDavImpl.scala`: Line 14 uses `mapError(UploadFailed.apply)` which loses the file name information
- `SardineScalaImpl.scala`: Error handling is inconsistent across methods
- `AuthServiceLive.scala`: Error handling in `authenticate` method is complex and could be simplified

## 5. Use Consistent Naming and Parameter Types

- `InvoiceStorage.scala`: Method `download` takes a parameter named `remotePath` but the implementation in
  `InvoiceWebDavImpl.scala` calls it `invoiceName`
- `InvoiceWebDavImpl.scala`: The `list` method returns hardcoded zeros for size and date (line 22)

## 6. Improve Logging

- Add structured logging with consistent patterns
- Replace string concatenation in log messages with structured logging

## 7. Refactor Long Methods

- `InvoiceServiceLive.scala`: The `createInvoice` method (lines 18-64) is too long and should be split into smaller
  functions

## 9. Use More Type Safety

- Replace raw strings with typed identifiers where appropriate
- Use newtypes or opaque types for domain concepts

## 10. Improve Test Coverage

- Add unit tests for error handling paths
- Add integration tests for the WebDAV implementation