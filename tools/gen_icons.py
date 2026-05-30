#!/usr/bin/env python3
"""Gera os icones (pixel art 16x16) do Smart Storage.

Sem dependencias externas: encoder PNG via zlib/struct da stdlib.
Saida: src/main/resources/assets/smart-storage/textures/gui/sprites/icon/*.png
       -> referenciaveis no codigo como  smart-storage:icon/<nome>  (blitSprite).

Cada icone recebe um contorno escuro automatico (auto_outline) para ficar
legivel por cima do botao cinza-medio do vanilla.
"""
import os, zlib, struct

SIZE = 16
OUT_DIR = os.path.join(os.path.dirname(__file__), "..",
                       "src/main/resources/assets/smart-storage/textures/gui/sprites/icon")

# ---- paleta RGBA -----------------------------------------------------------
T   = (0, 0, 0, 0)            # transparente
OUT = (33, 35, 43, 255)       # contorno escuro
WHT = (237, 240, 247, 255)
LGY = (176, 183, 197, 255)
DGY = (72, 78, 94, 255)
BLU = (110, 168, 255, 255)
BLD = (58, 104, 170, 255)
GRN = (120, 205, 128, 255)
GRD = (58, 150, 82, 255)
YEL = (255, 201, 92, 255)
ORG = (228, 134, 66, 255)
RED = (216, 92, 84, 255)
PUR = (170, 132, 238, 255)
CHS = (165, 114, 62, 255)     # bau
CHL = (198, 150, 98, 255)     # bau claro
CHD = (110, 72, 38, 255)      # bau escuro
STL = (158, 166, 184, 255)    # aco (cadeado/caixa)
STD = (96, 104, 124, 255)
GLD = (240, 198, 92, 255)     # dourado (arco do cadeado)
EDR = (42, 54, 60, 255)       # bau do end (escuro)
ENG = (80, 218, 186, 255)     # olho do ender (ciano)
SHK = (180, 154, 200, 255)    # shulker
SHD = (126, 104, 152, 255)    # shulker escuro
STN = (134, 136, 142, 255)    # pedra (ejetor/dropper)
STZ = (92, 94, 100, 255)      # pedra escura

# ---- helpers de desenho ----------------------------------------------------
def new():
    return [[T for _ in range(SIZE)] for _ in range(SIZE)]

def sp(g, x, y, c):
    if 0 <= x < SIZE and 0 <= y < SIZE:
        g[y][x] = c

def rect(g, x0, y0, x1, y1, c):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            sp(g, x, y, c)

def hl(g, y, x0, x1, c):
    for x in range(x0, x1 + 1):
        sp(g, x, y, c)

def vl(g, x, y0, y1, c):
    for y in range(y0, y1 + 1):
        sp(g, x, y, c)

def head_down(g, cx, y, c):
    hl(g, y,     cx - 3, cx + 4, c)
    hl(g, y + 1, cx - 2, cx + 3, c)
    hl(g, y + 2, cx - 1, cx + 2, c)
    hl(g, y + 3, cx,     cx + 1, c)

def head_up(g, cx, y, c):
    hl(g, y,     cx,     cx + 1, c)
    hl(g, y + 1, cx - 1, cx + 2, c)
    hl(g, y + 2, cx - 2, cx + 3, c)
    hl(g, y + 3, cx - 3, cx + 4, c)

def auto_outline(g, c=OUT):
    src = [row[:] for row in g]
    for y in range(SIZE):
        for x in range(SIZE):
            if src[y][x][3] != 0:
                continue
            for dx in (-1, 0, 1):
                for dy in (-1, 0, 1):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < SIZE and 0 <= ny < SIZE and src[ny][nx][3] != 0:
                        g[y][x] = c
                        break
                else:
                    continue
                break

# ---- icones ----------------------------------------------------------------
def emblem():  # marca / aba lateral: lista ordenada (barras decrescentes)
    g = new()
    hl(g, 3, 3, 13, BLU); hl(g, 4, 3, 13, BLD)
    hl(g, 7, 3, 10, BLU); hl(g, 8, 3, 10, BLD)
    hl(g, 11, 3, 7, BLU); hl(g, 12, 3, 7, BLD)
    return g

def tab_inventory():  # grade 3x3
    g = new()
    for cy in (3, 7, 11):
        for cx in (3, 7, 11):
            rect(g, cx, cy, cx + 2, cy + 2, LGY)
            hl(g, cy, cx, cx + 2, WHT)
    return g

def container_chest():  # bau
    g = new()
    rect(g, 2, 5, 13, 13, CHS)
    hl(g, 5, 2, 13, CHL)        # topo claro
    hl(g, 8, 2, 13, CHD)        # tampa/seam
    rect(g, 7, 7, 8, 9, GLD)    # fecho
    return g

def container_barrel():  # barril: madeira com aros horizontais e tampa
    g = new()
    rect(g, 3, 2, 12, 13, CHS)
    hl(g, 2, 5, 10, CHL)             # tampa clara no topo
    rect(g, 6, 2, 9, 3, CHD)         # encaixe da tampa
    rect(g, 3, 4, 12, 5, CHD)        # aro superior (forte)
    rect(g, 3, 10, 12, 11, CHD)      # aro inferior (forte)
    return g

def container_ender_chest():  # bau do end: escuro com olho ciano
    g = new()
    rect(g, 2, 5, 13, 13, EDR)
    hl(g, 5, 2, 13, ENG)             # linha do olho/tampa brilhando
    hl(g, 8, 2, 13, OUT)
    rect(g, 7, 7, 8, 9, ENG)         # olho central
    return g

def container_shulker():  # caixa de shulker: corpo + tampa (concha)
    g = new()
    rect(g, 3, 7, 12, 13, SHK)       # corpo
    rect(g, 4, 3, 11, 8, SHD)        # tampa/concha
    rect(g, 5, 4, 10, 6, SHK)        # brilho da tampa
    return g

def container_hopper():  # funil: tremonha (trapezio) + bico
    g = new()
    hl(g, 3, 2, 13, STL)             # borda larga do topo
    for i, y in enumerate(range(4, 8)):
        rect(g, 2 + i, y, 13 - i, y, STL)   # afunila
    rect(g, 3, 4, 12, 6, STZ)        # interior escuro
    rect(g, 6, 8, 9, 10, STL)        # corpo
    rect(g, 7, 10, 8, 12, STL)       # bico
    return g

def container_dispenser():  # ejetor: bloco de pedra com furo redondo
    g = new()
    rect(g, 2, 2, 13, 13, STN)
    rect(g, 6, 5, 9, 10, STZ)        # furo (eixo vertical)
    rect(g, 5, 6, 10, 9, STZ)        # furo (eixo horizontal) -> circulo
    return g

def container_dropper():  # dropper: bloco de pedra com furo quadrado
    g = new()
    rect(g, 2, 2, 13, 13, STN)
    rect(g, 6, 6, 9, 9, STZ)         # furo quadrado
    return g

def container_generic():  # container generico: caixote neutro com cintas
    g = new()
    rect(g, 2, 3, 13, 13, STN)
    rect(g, 2, 7, 13, 8, STZ)        # cinta horizontal
    rect(g, 7, 3, 8, 13, STZ)        # cinta vertical
    return g

def _letterA(g, x, y, c):
    hl(g, y, x + 1, x + 2, c)
    sp(g, x, y + 1, c); sp(g, x + 3, y + 1, c)
    hl(g, y + 2, x, x + 3, c)
    sp(g, x, y + 3, c); sp(g, x + 3, y + 3, c)
    sp(g, x, y + 4, c); sp(g, x + 3, y + 4, c)

def _letterZ(g, x, y, c):
    hl(g, y, x, x + 3, c)
    sp(g, x + 2, y + 1, c)
    sp(g, x + 1, y + 2, c)
    sp(g, x, y + 3, c)
    hl(g, y + 4, x, x + 3, c)

def sort_name_asc():   # A no topo, Z embaixo, seta p/ baixo
    g = new()
    _letterA(g, 2, 2, WHT)
    _letterZ(g, 2, 9, WHT)
    vl(g, 11, 2, 8, BLU); vl(g, 12, 2, 8, BLU)
    head_down(g, 11, 8, BLU)
    return g

def sort_name_desc():  # Z no topo, A embaixo, seta p/ cima
    g = new()
    _letterZ(g, 2, 2, WHT)
    _letterA(g, 2, 9, WHT)
    vl(g, 11, 7, 13, BLU); vl(g, 12, 7, 13, BLU)
    head_up(g, 11, 4, BLU)
    return g

def sort_count_asc():   # barras crescentes
    g = new()
    rect(g, 3, 11, 4, 13, BLU)
    rect(g, 6, 8, 7, 13, BLU)
    rect(g, 9, 5, 10, 13, BLU)
    rect(g, 12, 2, 13, 13, BLU)
    return g

def sort_count_desc():  # barras decrescentes
    g = new()
    rect(g, 3, 2, 4, 13, BLU)
    rect(g, 6, 5, 7, 13, BLU)
    rect(g, 9, 8, 10, 13, BLU)
    rect(g, 12, 11, 13, 13, BLU)
    return g

def sort_category():  # blocos coloridos agrupados
    g = new()
    rect(g, 2, 2, 5, 5, GRN)
    rect(g, 9, 3, 12, 6, YEL)
    rect(g, 5, 9, 9, 13, BLU)
    return g

def sort_mod():  # peca de quebra-cabeca
    g = new()
    rect(g, 3, 5, 12, 13, PUR)
    rect(g, 6, 2, 9, 5, PUR)     # encaixe topo
    rect(g, 12, 7, 14, 10, PUR)  # encaixe direita
    rect(g, 3, 7, 4, 10, T)      # soquete esquerda
    return g

def action_compact():  # dois triangulos comprimindo para o centro
    g = new()
    head_down(g, 7, 1, BLU)    # triangulo p/ baixo no topo (apice y4)
    head_up(g, 7, 11, BLU)     # triangulo p/ cima embaixo (apice y11)
    rect(g, 4, 7, 11, 8, DGY)  # linha central (alvo da compressao)
    return g

def _tray(g):  # caixa aberta embaixo
    rect(g, 3, 10, 12, 13, STD)
    vl(g, 3, 9, 13, STL); vl(g, 12, 9, 13, STL)
    hl(g, 13, 3, 12, STL)

def action_quick_stack():  # seta para baixo entrando na caixa
    g = new()
    _tray(g)
    vl(g, 7, 2, 7, GRN); vl(g, 8, 2, 7, GRN)
    head_down(g, 7, 6, GRN)
    return g

def action_deposit():  # tudo: chevron duplo para baixo na caixa
    g = new()
    _tray(g)
    head_down(g, 7, 2, GRN)
    head_down(g, 7, 5, GRN)
    return g

def action_withdraw():  # pegar tudo: chevron duplo para cima saindo da caixa
    g = new()
    _tray(g)
    head_up(g, 7, 3, ORG)
    head_up(g, 7, 6, ORG)
    return g

def action_pull():  # seta para cima saindo da caixa
    g = new()
    _tray(g)
    vl(g, 7, 8, 13, ORG); vl(g, 8, 8, 13, ORG)
    head_up(g, 7, 5, ORG)
    return g

def _lock_body(g):
    rect(g, 4, 8, 11, 14, STL)
    rect(g, 4, 12, 11, 14, STD)
    rect(g, 7, 10, 8, 11, OUT)   # buraco da fechadura
    sp(g, 7, 12, OUT)

def lock_closed():  # cadeado fechado (hotbar protegida)
    g = new()
    _lock_body(g)
    hl(g, 4, 6, 9, GLD)
    sp(g, 5, 5, GLD); sp(g, 10, 5, GLD)
    vl(g, 5, 6, 8, GLD); vl(g, 10, 6, 8, GLD)
    return g

def lock_open():  # cadeado aberto (hotbar livre): perna esquerda no corpo, direita erguida/solta
    g = new()
    _lock_body(g)
    vl(g, 5, 3, 8, GLD)          # perna esquerda entra no corpo (dobradica)
    sp(g, 5, 2, GLD)
    hl(g, 2, 6, 9, GLD)          # topo do arco
    sp(g, 9, 2, GLD)
    vl(g, 9, 2, 4, GLD)          # perna direita curta, termina no ar (gap grande -> aberto)
    return g

ICONS = {
    "emblem": emblem,
    "tab_inventory": tab_inventory,
    "container_chest": container_chest,
    "container_barrel": container_barrel,
    "container_ender_chest": container_ender_chest,
    "container_shulker": container_shulker,
    "container_hopper": container_hopper,
    "container_dispenser": container_dispenser,
    "container_dropper": container_dropper,
    "container_generic": container_generic,
    "sort_name_asc": sort_name_asc,
    "sort_name_desc": sort_name_desc,
    "sort_count_asc": sort_count_asc,
    "sort_count_desc": sort_count_desc,
    "sort_category": sort_category,
    "sort_mod": sort_mod,
    "action_compact": action_compact,
    "action_quick_stack": action_quick_stack,
    "action_pull": action_pull,
    "action_deposit": action_deposit,
    "action_withdraw": action_withdraw,
    "lock_closed": lock_closed,
    "lock_open": lock_open,
}

# ---- PNG encoder (stdlib) --------------------------------------------------
def write_png(path, grid):
    raw = bytearray()
    for row in grid:
        raw.append(0)  # filter type 0
        for (r, g, b, a) in row:
            raw += bytes((r, g, b, a))

    def chunk(tag, data):
        return (struct.pack(">I", len(data)) + tag + data
                + struct.pack(">I", zlib.crc32(tag + data) & 0xffffffff))

    ihdr = struct.pack(">IIBBBBB", SIZE, SIZE, 8, 6, 0, 0, 0)
    png = (b"\x89PNG\r\n\x1a\n"
           + chunk(b"IHDR", ihdr)
           + chunk(b"IDAT", zlib.compress(bytes(raw), 9))
           + chunk(b"IEND", b""))
    with open(path, "wb") as f:
        f.write(png)

def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    for name, fn in ICONS.items():
        g = fn()
        auto_outline(g)
        # validacao basica de tamanho
        assert len(g) == SIZE and all(len(r) == SIZE for r in g), name
        write_png(os.path.join(OUT_DIR, name + ".png"), g)
    print(f"{len(ICONS)} icones gerados em {os.path.normpath(OUT_DIR)}")

if __name__ == "__main__":
    main()
