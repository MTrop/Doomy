Doomy (C) Matt Tropiano
=========================
by Matt Tropiano et al. (see AUTHORS.txt)


0.8.0
-----

- `Fixed` Attempting to start the GUI in a headless environment should error out (Issue #5).
- `Fixed` Disabled "DPI Awareness" so that the OS can set DPI settings properly (Issue #7).
- `Fixed` Doomy would enter an infinite loop if unrecognized tokens were read in the default command.
- `Added` Name suffixes can be added to IWADs added via scan (Enh. #11).
- `Added` Name suffixes can be added to WADs added via scan.
- `Added` New Themes to the theme chooser (Enh. #6, kinda).
- `Changed` File choosers use the native implementations, now.


0.7.0
-----

- `Fixed` Deleting a single WAD would cause a SQL Exception.
- `Added` Default command line behavior for running presets/engines (Enh. #3).


0.6.2
-----

- `Added` JSoup version to help dialog.


0.6.1
-----

- `Removed` Unimplemented command `doomfetch` from help output.


0.6.0
-----

- Initial release.
