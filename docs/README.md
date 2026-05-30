# docs/

Media and assets used by the project's `README.md` and the Modrinth/CurseForge pages.

| Path | What it is |
| --- | --- |
| `icon.png` | Project icon used in the README header and on mod listing pages. |
| `demo.gif` | Short, optimized clip of the panel in action (embedded in the README). |
| `screenshots/` | Static screenshots (`chest.png`, `inv.png`) shown in the README. |
| `icons/` | Stable copies of the in-game UI icons (sort, lock, deposit/withdraw, …), exported by `tools/gen_icons.py` for the Modrinth page. |

## Updating the demo

`demo.gif` is a compressed export of a raw screen recording. The full-resolution
source is intentionally **not** committed (it is hundreds of MB). To regenerate
from a new capture with `ffmpeg`:

```sh
# Two-pass palette keeps the file small without banding.
ffmpeg -i capture.mp4 -vf "fps=15,scale=720:-1:flags=lanczos,palettegen=stats_mode=diff" /tmp/pal.png
ffmpeg -i capture.mp4 -i /tmp/pal.png \
  -lavfi "fps=15,scale=720:-1:flags=lanczos[x];[x][1:v]paletteuse=dither=bayer:bayer_scale=4" \
  docs/demo.gif
```

Aim for a few MB so it loads quickly in the README.
