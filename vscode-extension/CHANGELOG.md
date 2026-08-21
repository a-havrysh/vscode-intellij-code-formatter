# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [2025.3.2] - 2026-02-05

### Fixed
- Fixed critical bug where files with CRLF (Windows) line endings would not format correctly
- Fixed range formatting not working with CRLF line endings
- Improved line ending detection and preservation
  - Formatter now detects original line endings (CRLF, LF, CR)
  - Normalizes to LF internally for IntelliJ Platform compatibility
  - Converts back to original line endings after formatting
- Fixed `getLineStartOffset()` and `getLineEndOffset()` methods to handle all line ending types

### Technical Details
- Added `detectLineEnding()` method to identify line ending style
- Added `normalizeLineEndings()` to convert all line endings to LF
- Added `convertLineEndings()` to restore original line endings after formatting
- Updated offset calculation methods to correctly handle CRLF sequences

## [2025.3.1] - Previous Release

Initial release with IntelliJ 2025.3.1 formatting engine support.
