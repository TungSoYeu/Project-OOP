Add-Type -AssemblyName System.Drawing

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$umlDir = Join-Path $root "uml"
New-Item -ItemType Directory -Force -Path $umlDir | Out-Null

function New-Canvas {
    param([int]$Width, [int]$Height)

    $bitmap = New-Object System.Drawing.Bitmap $Width, $Height
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
    $graphics.Clear([System.Drawing.Color]::White)
    return @($bitmap, $graphics)
}

function New-Font {
    param(
        [float]$Size,
        [System.Drawing.FontStyle]$Style = [System.Drawing.FontStyle]::Regular,
        [string]$Family = "Segoe UI"
    )
    return New-Object System.Drawing.Font $Family, $Size, $Style
}

function Draw-Title {
    param($G, [string]$Text, [int]$Width)

    $font = New-Font 28 ([System.Drawing.FontStyle]::Bold)
    $brush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::Black)
    $format = New-Object System.Drawing.StringFormat
    $format.Alignment = [System.Drawing.StringAlignment]::Center
    $format.LineAlignment = [System.Drawing.StringAlignment]::Center
    $G.DrawString($Text, $font, $brush, [System.Drawing.RectangleF]::new(0, 20, $Width, 55), $format)
    $font.Dispose()
    $brush.Dispose()
    $format.Dispose()
}

function Draw-UmlClass {
    param(
        $G,
        [int]$X,
        [int]$Y,
        [int]$W,
        [int]$H,
        [string]$Name,
        [string[]]$Attributes = @(),
        [string[]]$Methods = @()
    )

    $pen = New-Object System.Drawing.Pen ([System.Drawing.Color]::Black), 2
    $thinPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::Black), 1
    $brush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::White)
    $textBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::Black)
    $titleFont = New-Font 14 ([System.Drawing.FontStyle]::Bold)
    $bodyFont = New-Font 10 ([System.Drawing.FontStyle]::Regular) "Consolas"

    $rect = [System.Drawing.Rectangle]::new($X, $Y, $W, $H)
    $G.FillRectangle($brush, $rect)
    $G.DrawRectangle($pen, $rect)

    $titleH = 36
    $attrH = if ($Methods.Count -gt 0) { [Math]::Min(28 + ($Attributes.Count * 18), [Math]::Max(42, $H - $titleH - 48)) } else { $H - $titleH }

    $G.DrawLine($thinPen, $X, $Y + $titleH, $X + $W, $Y + $titleH)
    if ($Methods.Count -gt 0) {
        $G.DrawLine($thinPen, $X, $Y + $titleH + $attrH, $X + $W, $Y + $titleH + $attrH)
    }

    $center = New-Object System.Drawing.StringFormat
    $center.Alignment = [System.Drawing.StringAlignment]::Center
    $center.LineAlignment = [System.Drawing.StringAlignment]::Center
    $G.DrawString($Name, $titleFont, $textBrush, [System.Drawing.RectangleF]::new($X + 4, $Y, $W - 8, $titleH), $center)

    $left = New-Object System.Drawing.StringFormat
    $left.Alignment = [System.Drawing.StringAlignment]::Near
    $left.LineAlignment = [System.Drawing.StringAlignment]::Near

    if ($Attributes.Count -gt 0) {
        $G.DrawString(($Attributes -join "`n"), $bodyFont, $textBrush, [System.Drawing.RectangleF]::new($X + 10, $Y + $titleH + 8, $W - 20, $attrH - 12), $left)
    }

    if ($Methods.Count -gt 0) {
        $G.DrawString(($Methods -join "`n"), $bodyFont, $textBrush, [System.Drawing.RectangleF]::new($X + 10, $Y + $titleH + $attrH + 8, $W - 20, $H - $titleH - $attrH - 12), $left)
    }

    $pen.Dispose()
    $thinPen.Dispose()
    $brush.Dispose()
    $textBrush.Dispose()
    $titleFont.Dispose()
    $bodyFont.Dispose()
    $center.Dispose()
    $left.Dispose()
}

function Draw-Package {
    param(
        $G,
        [int]$X,
        [int]$Y,
        [int]$W,
        [int]$H,
        [string]$Name,
        [string[]]$Lines
    )

    Draw-UmlClass $G $X $Y $W $H $Name $Lines @()
}

function Draw-Polyline {
    param(
        $G,
        [int[][]]$Points,
        [bool]$Dashed = $false
    )

    $pen = New-Object System.Drawing.Pen ([System.Drawing.Color]::Black), 2
    if ($Dashed) {
        $pen.DashStyle = [System.Drawing.Drawing2D.DashStyle]::Dash
    }
    for ($i = 0; $i -lt $Points.Count - 1; $i++) {
        $G.DrawLine($pen, $Points[$i][0], $Points[$i][1], $Points[$i + 1][0], $Points[$i + 1][1])
    }
    $pen.Dispose()
}

function Draw-OpenArrow {
    param(
        $G,
        [int[][]]$Points
    )

    Draw-Polyline $G $Points

    $last = $Points[$Points.Count - 1]
    $prev = $Points[$Points.Count - 2]
    $dx = $last[0] - $prev[0]
    $dy = $last[1] - $prev[1]
    $len = [Math]::Sqrt(($dx * $dx) + ($dy * $dy))
    if ($len -eq 0) { return }

    $ux = $dx / $len
    $uy = $dy / $len
    $px = -$uy
    $py = $ux
    $size = 13

    $p1 = [System.Drawing.Point]::new([int]($last[0] - $ux * $size + $px * $size / 2), [int]($last[1] - $uy * $size + $py * $size / 2))
    $p2 = [System.Drawing.Point]::new([int]($last[0]), [int]($last[1]))
    $p3 = [System.Drawing.Point]::new([int]($last[0] - $ux * $size - $px * $size / 2), [int]($last[1] - $uy * $size - $py * $size / 2))

    $pen = New-Object System.Drawing.Pen ([System.Drawing.Color]::Black), 2
    $arrowPoints = [System.Drawing.Point[]]@($p1, $p2, $p3)
    $G.DrawLines($pen, $arrowPoints)
    $pen.Dispose()
}

function Draw-InheritanceBus {
    param(
        $G,
        [int]$ParentX,
        [int]$ParentY,
        [int]$BusY,
        [int[]]$ChildXs,
        [int]$ChildY
    )

    $pen = New-Object System.Drawing.Pen ([System.Drawing.Color]::Black), 2
    $brush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::White)

    $tri = [System.Drawing.Point[]]@(
        [System.Drawing.Point]::new($ParentX, $ParentY),
        [System.Drawing.Point]::new($ParentX - 12, $ParentY + 20),
        [System.Drawing.Point]::new($ParentX + 12, $ParentY + 20)
    )
    $G.FillPolygon($brush, $tri)
    $G.DrawPolygon($pen, $tri)
    $G.DrawLine($pen, $ParentX, $ParentY + 20, $ParentX, $BusY)

    $minX = ($ChildXs | Measure-Object -Minimum).Minimum
    $maxX = ($ChildXs | Measure-Object -Maximum).Maximum
    $G.DrawLine($pen, $minX, $BusY, $maxX, $BusY)
    foreach ($x in $ChildXs) {
        $G.DrawLine($pen, $x, $BusY, $x, $ChildY)
    }

    $pen.Dispose()
    $brush.Dispose()
}

function Save-Canvas {
    param($Bitmap, $Graphics, [string]$Path)

    $Graphics.Dispose()
    $Bitmap.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
    $Bitmap.Dispose()
}

function New-PackageDiagram {
    $canvas = New-Canvas 1900 1150
    $bmp = $canvas[0]
    $g = $canvas[1]
    Draw-Title $g "Package Dependency Diagram - Wild-Life Eco Simulation" 1900

    Draw-Package $g 760 110 380 135 "com.ecosim" @("App", "TestEngine")
    Draw-Package $g 120 360 360 170 "com.ecosim.engine" @("SimulationEngine", "EntityManager", "SeasonManager")
    Draw-Package $g 750 350 420 230 "com.ecosim.model" @("Entity, Animal, Plant", "Rabbit, Deer, Wolf, Tiger", "Elephant, Hunter, Fish, Duck", "Grass, FruitTree", "WorldMap, TerrainTile", "Season, TerrainType, Action")
    Draw-Package $g 1380 360 370 185 "com.ecosim.view" @("GameView, Camera", "Renderer", "BasicRenderer", "SpriteRenderer", "AssetManager", "ParticleSystem")
    Draw-Package $g 180 760 390 190 "com.ecosim.strategy" @("SurvivalStrategy", "PassiveStrategy", "ScaredStrategy", "HunterStrategy", "AggressiveStrategy")
    Draw-Package $g 780 790 300 130 "com.ecosim.util" @("Constants", "Vector2D")
    Draw-Package $g 1380 790 300 130 "com.ecosim.sound" @("SoundManager", "WAV resources")

    # Root dependencies.
    Draw-OpenArrow $g @(@(850,245), @(850,300), @(300,300), @(300,360))
    Draw-OpenArrow $g @(@(950,245), @(950,350))
    Draw-OpenArrow $g @(@(1050,245), @(1050,300), @(1565,300), @(1565,360))

    # Engine dependencies. Lines use side channels to avoid boxes.
    Draw-OpenArrow $g @(@(480,445), @(615,445), @(615,465), @(750,465))
    Draw-OpenArrow $g @(@(300,530), @(300,680), @(375,680), @(375,760))
    Draw-OpenArrow $g @(@(390,530), @(390,705), @(870,705), @(870,790))

    # Strategy and model dependency.
    Draw-OpenArrow $g @(@(570,840), @(655,840), @(655,535), @(750,535))

    # View dependencies.
    Draw-OpenArrow $g @(@(1380,445), @(1170,445))
    Draw-OpenArrow $g @(@(1565,545), @(1565,790))
    Draw-OpenArrow $g @(@(1485,545), @(1485,680), @(930,680), @(930,790))

    # Model to util.
    Draw-OpenArrow $g @(@(960,580), @(960,685), @(930,685), @(930,790))

    Save-Canvas $bmp $g (Join-Path $umlDir "package-diagram.png")
}

function New-ClassDiagram {
    $canvas = New-Canvas 2850 1950
    $bmp = $canvas[0]
    $g = $canvas[1]
    Draw-Title $g "Class Diagram - Wild-Life Eco Simulation" 2850

    Draw-UmlClass $g 1120 95 460 230 "Entity" @("- id: String", "- name: String", "# position: Vector2D", "# alive: boolean", "# priority: int", "# size: double") @("+ update(dt, map): void", "+ canTraverse(t): boolean", "+ getTypeName(): String")

    Draw-UmlClass $g 450 390 620 250 "Animal" @("# hunger: double", "# thirst: double", "# health: double", "# speed: double", "# direction: Vector2D", "# strategy: SurvivalStrategy", "# state: AnimalState") @("+ executeAction(a, dt, map): void", "+ canReproduce(): boolean", "+ createOffspring(): Animal", "+ setStrategy(s): void")

    Draw-UmlClass $g 1770 405 520 215 "Plant" @("# nutrition: double", "# growth: double", "# spreadTimer: double") @("+ beEaten(dt): double", "+ canSpread(): boolean", "+ createOffspring(pos): Plant")

    Draw-UmlClass $g 70 770 220 160 "Rabbit" @("+ naturalEnemies", "+ preyTypes") @("+ canTraverse(t): boolean", "+ createOffspring(): Animal")
    Draw-UmlClass $g 330 770 220 160 "Deer" @("+ naturalEnemies", "+ preyTypes") @("+ canTraverse(t): boolean", "+ createOffspring(): Animal")
    Draw-UmlClass $g 590 770 220 160 "Wolf" @("+ preyTypes", "+ naturalEnemies") @("+ canTraverse(t): boolean", "+ createOffspring(): Animal")
    Draw-UmlClass $g 850 770 220 160 "Tiger" @("+ preyTypes", "+ ambushDamage") @("+ canTraverse(t): boolean", "+ createOffspring(): Animal")
    Draw-UmlClass $g 1110 770 220 160 "Elephant" @("+ preyTypes", "+ priority = 10") @("+ canTraverse(t): boolean", "+ createOffspring(): Animal")
    Draw-UmlClass $g 1370 770 220 160 "Hunter" @("+ preyTypes", "+ sightRange") @("+ canTraverse(t): boolean", "+ getTypeName(): String")
    Draw-UmlClass $g 1630 770 220 160 "Fish" @("+ defaultStrategy", "+ waterOnly") @("+ canTraverse(t): boolean", "+ createOffspring(): Animal")
    Draw-UmlClass $g 1890 770 220 160 "Duck" @("+ defaultStrategy", "+ preyTypes") @("+ canTraverse(t): boolean", "+ createOffspring(): Animal")

    Draw-UmlClass $g 2310 770 220 145 "Grass" @("+ growth") @("+ getTypeName(): String")
    Draw-UmlClass $g 2570 770 220 145 "FruitTree" @("+ fruitAmount") @("+ getTypeName(): String")

    Draw-UmlClass $g 110 1130 370 220 "SurvivalStrategy" @("<<interface>>") @("+ decide(self, nearby, map): Action", "+ getName(): String")
    Draw-UmlClass $g 50 1510 250 135 "PassiveStrategy" @("- random") @("+ decide(...): Action")
    Draw-UmlClass $g 340 1510 250 135 "ScaredStrategy" @("- safeMode") @("+ decide(...): Action")
    Draw-UmlClass $g 630 1510 250 135 "HunterStrategy" @("- random") @("+ decide(...): Action")
    Draw-UmlClass $g 920 1510 250 135 "AggressiveStrategy" @("- hungerThreshold") @("+ decide(...): Action")

    Draw-UmlClass $g 1230 1130 430 230 "SimulationEngine" @("- worldMap: WorldMap", "- entityManager: EntityManager", "- seasonManager: SeasonManager", "- running: boolean") @("+ tick(dt): void", "+ spawnEntity(type, x, y): void", "+ plantGrass(x, y): void")
    Draw-UmlClass $g 1780 1130 420 220 "EntityManager" @("- entities: List<Entity>", "- worldMap: WorldMap", "- random: Random") @("+ spawnInitialEntities(): void", "+ getNearby(e, r): List<Entity>", "+ processSpringReproduction(): void", "+ resolveMovementPriority(): void")
    Draw-UmlClass $g 2320 1130 320 170 "SeasonManager" @("- currentSeason: Season", "- elapsedTime: double") @("+ update(dt): void", "+ getSeasonProgress(): double")

    Draw-UmlClass $g 1330 1510 340 180 "WorldMap" @("- tiles: TerrainTile[][]", "- random: Random") @("+ getTerrainAt(x, y): TerrainType", "+ setTerrainAt(x, y, t): void", "+ getRandomPosition(t): Vector2D")
    Draw-UmlClass $g 1770 1510 300 145 "TerrainTile" @("- type: TerrainType") @("+ getType(): TerrainType")
    Draw-UmlClass $g 2160 1510 300 145 "Renderer" @("<<interface>>") @("+ renderTerrain(...): void", "+ renderEntity(...): void", "+ getModeName(): String")
    Draw-UmlClass $g 2110 1740 250 120 "BasicRenderer" @() @("+ renderEntity(...): void")
    Draw-UmlClass $g 2400 1740 250 120 "SpriteRenderer" @("- baseRenderer") @("+ renderEntity(...): void")

    Draw-UmlClass $g 2440 405 330 185 "Utilities" @("Constants", "Vector2D", "AssetManager", "SoundManager") @()
    Draw-UmlClass $g 1320 980 330 135 "AnimalReproductionFactory" @("<<utility>>") @("+ createOffspring(parent, map): Animal")
    Draw-UmlClass $g 2480 1360 310 145 "GameView" @("- engine: SimulationEngine", "- renderer: Renderer", "- camera: Camera") @("+ startGameLoop(): void", "+ render(): void")

    # Inheritance lines.
    Draw-InheritanceBus $g 1350 325 365 @(760,2030) 390
    Draw-InheritanceBus $g 760 640 710 @(180,440,700,960,1220,1480,1740,2000) 770
    Draw-InheritanceBus $g 2030 620 710 @(2420,2680) 770
    Draw-InheritanceBus $g 295 1350 1435 @(175,465,755,1045) 1510
    Draw-InheritanceBus $g 2310 1655 1705 @(2235,2525) 1740

    # Associations and composition. Routed around boxes.
    Draw-Polyline $g @(@(450,505), @(25,505), @(25,1240), @(110,1240)) $true

    Draw-Polyline $g @(@(1450,1360), @(1450,1510))
    Draw-Polyline $g @(@(1660,1210), @(1780,1210))
    Draw-Polyline $g @(@(1660,1190), @(1710,1190), @(1710,940), @(2480,940), @(2480,1130))
    Draw-Polyline $g @(@(1990,1350), @(1990,1510))
    Draw-Polyline $g @(@(1860,1350), @(1860,1460), @(1670,1460), @(1670,1600))
    Draw-Polyline $g @(@(1990,1130), @(1990,1045), @(1650,1045))

    Draw-Polyline $g @(@(2480,1430), @(2260,1430), @(2260,1385), @(1450,1385), @(1450,1360))
    Draw-Polyline $g @(@(2635,1505), @(2635,1685), @(2310,1685), @(2310,1655))

    Draw-Polyline $g @(@(1580,195), @(2390,195), @(2390,497), @(2440,497)) $true

    Save-Canvas $bmp $g (Join-Path $umlDir "class-diagram.png")
}

New-PackageDiagram
New-ClassDiagram

Write-Host "Generated:"
Write-Host (Join-Path $umlDir "package-diagram.png")
Write-Host (Join-Path $umlDir "class-diagram.png")
