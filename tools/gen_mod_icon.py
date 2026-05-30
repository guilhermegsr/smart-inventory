#!/usr/bin/env python3
"""Gera o icone do mod (128x128) desenhado a 32x32 e ampliado 4x (nearest).

Baú (tema de armazenamento) + emblema de ordenacao (marca do mod) sobre painel escuro.
Saida: src/main/resources/assets/smart-storage/icon.png
"""
import os, zlib, struct

S = 32
SCALE = 4
OUT = os.path.join(os.path.dirname(__file__), "..", "src/main/resources/assets/smart-storage/icon.png")

T   = (0, 0, 0, 0)
BG  = (33, 37, 47, 255)
BG2 = (24, 27, 35, 255)
BORDER = (78, 86, 108, 255)
OUTL = (18, 20, 26, 255)
CHS = (165, 114, 62, 255)
CHL = (202, 154, 100, 255)
CHD = (108, 70, 36, 255)
GLD = (240, 198, 92, 255)
BLU = (110, 168, 255, 255)
BLD = (58, 104, 170, 255)


def new():
    return [[T for _ in range(S)] for _ in range(S)]

def sp(g, x, y, c):
    if 0 <= x < S and 0 <= y < S:
        g[y][x] = c

def rect(g, x0, y0, x1, y1, c):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            sp(g, x, y, c)

def auto_outline(g, c):
    src = [row[:] for row in g]
    for y in range(S):
        for x in range(S):
            if src[y][x][3] != 0:
                continue
            for dx in (-1, 0, 1):
                for dy in (-1, 0, 1):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < S and 0 <= ny < S and src[ny][nx][3] != 0:
                        g[y][x] = c
                        break
                else:
                    continue
                break


def lerp(a, b, t):
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(4))

def background():
    g = new()
    for y in range(S):  # gradiente vertical suave
        rect(g, 0, y, S - 1, y, lerp(BG, BG2, y / (S - 1)))
    # borda
    rect(g, 0, 0, S - 1, 0, BORDER); rect(g, 0, S - 1, S - 1, S - 1, BORDER)
    rect(g, 0, 0, 0, S - 1, BORDER); rect(g, S - 1, 0, S - 1, S - 1, BORDER)
    # cantos arredondados (2px)
    for cx, cy, dx, dy in [(0, 0, 1, 1), (S - 1, 0, -1, 1), (0, S - 1, 1, -1), (S - 1, S - 1, -1, -1)]:
        sp(g, cx, cy, T); sp(g, cx + dx, cy, T); sp(g, cx, cy + dy, T)
        sp(g, cx + dx, cy + dy, BORDER)
    return g


def foreground():
    g = new()
    # emblema: 3 barras azuis decrescentes (marca do mod)
    rect(g, 8, 5, 23, 6, BLU);  rect(g, 8, 6, 23, 6, BLD)
    rect(g, 8, 9, 19, 10, BLU); rect(g, 8, 10, 19, 10, BLD)
    rect(g, 8, 13, 15, 14, BLU); rect(g, 8, 14, 15, 14, BLD)
    # bau
    rect(g, 7, 18, 24, 28, CHS)
    rect(g, 7, 18, 24, 19, CHL)     # topo claro
    rect(g, 7, 21, 24, 22, CHD)     # divisa tampa/corpo
    rect(g, 14, 23, 17, 26, GLD)    # fecho
    auto_outline(g, OUTL)
    return g


def compose():
    bg = background()
    fg = foreground()
    for y in range(S):
        for x in range(S):
            if fg[y][x][3] != 0:
                bg[y][x] = fg[y][x]
    return bg


def write_png(path, grid):
    raw = bytearray()
    for row in grid:
        for _ in range(SCALE):  # replica cada linha
            raw.append(0)
            for px in row:
                raw += bytes(px) * SCALE  # replica cada pixel
    w = h = S * SCALE

    def chunk(tag, data):
        return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xffffffff)

    png = (b"\x89PNG\r\n\x1a\n"
           + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
           + chunk(b"IDAT", zlib.compress(bytes(raw), 9))
           + chunk(b"IEND", b""))
    with open(path, "wb") as f:
        f.write(png)


if __name__ == "__main__":
    write_png(os.path.normpath(OUT), compose())
    print("icone gerado:", os.path.normpath(OUT))
