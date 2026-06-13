$ErrorActionPreference = "Stop"

$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$template = "C:\Users\PC\Downloads\HUST_PPT_template_2022_RED_16x9_567042.pptx"
$out = Join-Path $root "Wild-Life_Eco_Simulation_OOP_HUST.pptx"

function Rgb($r, $g, $b) {
    return $r + ($g -shl 8) + ($b -shl 16)
}

$Black = Rgb 0 0 0
$White = Rgb 255 255 255
$DarkGray = Rgb 55 55 55
$MidGray = Rgb 110 110 110
$LightGray = Rgb 242 242 242
$HustRed = Rgb 180 0 0
$SoftRed = Rgb 245 232 232

function Set-TextStyle($shape, $fontSize, $color, $bold = $false) {
    $tr = $shape.TextFrame.TextRange
    $tr.Font.Name = "Arial"
    $tr.Font.Size = [int][Math]::Round($fontSize)
    $tr.Font.Color.RGB = $color
    $tr.Font.Bold = if ($bold) { -1 } else { 0 }
}

function Add-TextBox($slide, $text, $x, $y, $w, $h, $fontSize = 20, $color = $Black, $bold = $false) {
    $shape = $slide.Shapes.AddTextbox(1, $x, $y, $w, $h)
    $shape.TextFrame.TextRange.Text = $text
    $shape.TextFrame.MarginLeft = 8
    $shape.TextFrame.MarginRight = 8
    $shape.TextFrame.MarginTop = 4
    $shape.TextFrame.MarginBottom = 4
    Set-TextStyle $shape $fontSize $color $bold
    return $shape
}

function Add-SlideTitle($slide, $title, $subtitle = "") {
    $line = $slide.Shapes.AddShape(1, 42, 80, 876, 4)
    $line.Fill.ForeColor.RGB = $HustRed
    $line.Line.Visible = 0

    Add-TextBox $slide $title 42 10 860 44 23 $White $true | Out-Null
    if ($subtitle -ne "") {
        Add-TextBox $slide $subtitle 42 62 820 24 11 $White $false | Out-Null
    }
}

function Add-Footer($slide, $num) {
    Add-TextBox $slide "Lập trình hướng đối tượng | Wild-Life Eco Simulation" 42 512 620 20 8 $MidGray $false | Out-Null
    $box = Add-TextBox $slide "$num" 890 512 30 20 8 $MidGray $false
    $box.TextFrame.TextRange.ParagraphFormat.Alignment = 3
}

function Add-Bullets($slide, [string[]]$items, $x, $y, $w, $h, $fontSize = 17) {
    $text = ($items | ForEach-Object { "• $_" }) -join "`r"
    $shape = Add-TextBox $slide $text $x $y $w $h $fontSize $Black $false
    $shape.TextFrame.TextRange.ParagraphFormat.SpaceAfter = 7
    return $shape
}

function Add-NumberCard($slide, $number, $label, $x, $y, $w, $h) {
    $shape = $slide.Shapes.AddShape(1, $x, $y, $w, $h)
    $shape.Fill.ForeColor.RGB = $White
    $shape.Line.ForeColor.RGB = $HustRed
    $shape.Line.Weight = 1.5

    Add-TextBox $slide $number ($x + 8) ($y + 8) ($w - 16) 36 24 $HustRed $true | Out-Null
    Add-TextBox $slide $label ($x + 8) ($y + 45) ($w - 16) ($h - 50) 10 $DarkGray $false | Out-Null
}

function Add-Callout($slide, $title, [string[]]$lines, $x, $y, $w, $h) {
    $shape = $slide.Shapes.AddShape(1, $x, $y, $w, $h)
    $shape.Fill.ForeColor.RGB = $LightGray
    $shape.Line.ForeColor.RGB = $HustRed
    $shape.Line.Weight = 1.25
    Add-TextBox $slide $title ($x + 10) ($y + 8) ($w - 20) 24 14 $HustRed $true | Out-Null
    Add-Bullets $slide $lines ($x + 10) ($y + 36) ($w - 20) ($h - 42) 11 | Out-Null
}

function Add-FitPicture($slide, $path, $x, $y, $w, $h) {
    if (Test-Path $path) {
        $pic = $slide.Shapes.AddPicture($path, 0, -1, $x, $y, -1, -1)
        $scale = [Math]::Min($w / $pic.Width, $h / $pic.Height)
        $pic.Width = $pic.Width * $scale
        $pic.Height = $pic.Height * $scale
        $pic.Left = $x + (($w - $pic.Width) / 2)
        $pic.Top = $y + (($h - $pic.Height) / 2)
        return $pic
    }

    $shape = $slide.Shapes.AddShape(1, $x, $y, $w, $h)
    $shape.Fill.ForeColor.RGB = $White
    $shape.Line.ForeColor.RGB = $MidGray
    $shape.Line.DashStyle = 4
    $name = Split-Path $path -Leaf
    $tb = Add-TextBox $slide "Chèn ảnh: $name" ($x + 12) ($y + ($h / 2) - 15) ($w - 24) 30 15 $MidGray $false
    $tb.TextFrame.TextRange.ParagraphFormat.Alignment = 2
    return $shape
}

function Add-ProcessStep($slide, $n, $title, $body, $x, $y, $w, $h) {
    $circle = $slide.Shapes.AddShape(9, $x, $y + 5, 36, 36)
    $circle.Fill.ForeColor.RGB = $HustRed
    $circle.Line.Visible = 0
    $num = Add-TextBox $slide "$n" ($x + 2) ($y + 8) 32 30 15 $White $true
    $num.TextFrame.TextRange.ParagraphFormat.Alignment = 2
    Add-TextBox $slide $title ($x + 46) $y ($w - 46) 26 14 $HustRed $true | Out-Null
    Add-TextBox $slide $body ($x + 46) ($y + 26) ($w - 46) ($h - 26) 10.5 $DarkGray $false | Out-Null
}

try {
$pp = New-Object -ComObject PowerPoint.Application
$presentation = $pp.Presentations.Open($template, 0, 0, 0)

# Keep master/theme, remove example slides.
for ($i = $presentation.Slides.Count; $i -ge 1; $i--) {
    $presentation.Slides.Item($i).Delete()
}

$blankLayout = $presentation.SlideMaster.CustomLayouts.Item(5)

function New-BlankSlide($presentation, $blankLayout) {
    return $presentation.Slides.AddSlide($presentation.Slides.Count + 1, $blankLayout)
}

# 1. Cover
$s = New-BlankSlide $presentation $blankLayout
$bar = $s.Shapes.AddShape(1, 0, 0, 960, 92)
$bar.Fill.ForeColor.RGB = $HustRed
$bar.Line.Visible = 0
Add-TextBox $s "BÁO CÁO BÀI TẬP LỚN" 56 130 820 34 21 $HustRed $true | Out-Null
Add-TextBox $s "WILD-LIFE ECO SIMULATION" 56 172 820 54 34 $Black $true | Out-Null
Add-TextBox $s "Môn: Lập trình hướng đối tượng" 56 238 620 30 18 $DarkGray $false | Out-Null
Add-TextBox $s "Hệ thống mô phỏng hệ sinh thái hoang dã bằng JavaFX" 56 274 760 30 16 $MidGray $false | Out-Null
Add-TextBox $s "Nhóm thực hiện: ...`rGiảng viên hướng dẫn: ...`rLớp: ..." 56 360 520 86 15 $Black $false | Out-Null
Add-TextBox $s "Đại học Bách khoa Hà Nội" 56 36 520 28 20 $White $true | Out-Null
Add-Footer $s 1

# 2. Agenda
$s = New-BlankSlide $presentation $blankLayout
Add-SlideTitle $s "Nội dung trình bày" "Tóm tắt cấu trúc bài báo cáo và luồng thuyết trình"
Add-Bullets $s @(
    "Giới thiệu đề tài và mục tiêu mô phỏng",
    "Dữ liệu mô phỏng và các thực thể trong hệ sinh thái",
    "Kiến trúc package và sơ đồ lớp",
    "Các kỹ thuật lập trình hướng đối tượng đã áp dụng",
    "Thuật toán, công nghệ sử dụng và kiểm thử",
    "Demo chương trình, kết luận và hướng phát triển"
) 90 125 760 300 20 | Out-Null
Add-Footer $s 2

# 3. Overview
$s = New-BlankSlide $presentation $blankLayout
Add-SlideTitle $s "Giới thiệu đề tài" "Wild-Life Eco Simulation mô phỏng một hệ sinh thái hoang dã có nhiều loài và nhiều loại địa hình"
Add-Callout $s "Bối cảnh" @(
    "Mô phỏng bản đồ 2D gồm đồng cỏ, rừng rậm, hồ nước, bùn, đá và bụi rậm.",
    "Mỗi thực thể có trạng thái riêng và được cập nhật liên tục theo thời gian.",
    "Người dùng có thể quan sát, thêm entity và thay đổi môi trường."
) 70 125 390 245
Add-Callout $s "Mục tiêu OOP" @(
    "Thiết kế hệ thống lớp rõ ràng, dễ mở rộng.",
    "Tách logic mô phỏng, hành vi và giao diện.",
    "Thể hiện đóng gói, kế thừa, đa hình, trừu tượng hóa và pattern."
) 500 125 390 245
Add-Footer $s 3

# 4. Features
$s = New-BlankSlide $presentation $blankLayout
Add-SlideTitle $s "Tính năng chính" "Các chức năng nổi bật trong phiên bản hiện tại"
Add-Bullets $s @(
    "Bản đồ 120 x 80 tile với 6 loại địa hình khác nhau.",
    "Hệ sinh thái gồm thực vật, động vật ăn cỏ, thú săn mồi, sinh vật nước và thợ săn.",
    "Động vật có máu, độ đói, độ khát, tốc độ, tầm nhìn, con mồi và kẻ thù.",
    "Mùa trong năm ảnh hưởng đến sinh sản và tốc độ phát triển của thực vật.",
    "Giao diện JavaFX hỗ trợ zoom, pan, thêm entity, đổi chế độ render và xem thống kê."
) 70 125 820 290 18 | Out-Null
Add-Footer $s 4

# 5. Data
$s = New-BlankSlide $presentation $blankLayout
Add-SlideTitle $s "Dữ liệu mô phỏng" "Dữ liệu được khởi tạo trong chương trình để tạo môi trường ban đầu"
Add-NumberCard $s "9600" "ô bản đồ`r120 x 80 tile" 75 130 180 110
Add-NumberCard $s "6" "loại địa hình`rgrassland, forest, water, mud, rock, bush" 285 130 180 110
Add-NumberCard $s "4" "mùa trong năm`rspring, summer, autumn, winter" 495 130 180 110
Add-NumberCard $s "228" "thực thể ban đầu`r150 thực vật + 78 động vật" 705 130 180 110
Add-Bullets $s @(
    "120 cỏ, 30 cây ăn quả, 30 thỏ, 15 hươu, 5 sói, 2 hổ, 2 voi, 1 thợ săn, 15 cá, 8 vịt.",
    "Dữ liệu tài nguyên gồm 9 ảnh sprite PNG và 6 tệp âm thanh WAV.",
    "Dữ liệu mô phỏng giúp kiểm tra tương tác giữa thức ăn, con mồi, thú săn mồi và địa hình."
) 90 285 780 160 16 | Out-Null
Add-Footer $s 5

# 6. Package diagram
$s = New-BlankSlide $presentation $blankLayout
Add-SlideTitle $s "Biểu đồ phụ thuộc gói" "Cấu trúc package được tách theo trách nhiệm"
Add-FitPicture $s (Join-Path $root "uml\package-diagram.png") 40 105 555 360 | Out-Null
Add-Bullets $s @(
    "com.ecosim khởi động ứng dụng và tạo engine/view.",
    "engine điều phối mô phỏng và phụ thuộc vào model, strategy.",
    "view chỉ chịu trách nhiệm hiển thị và tương tác người dùng.",
    "util chứa hằng số và vector dùng chung."
) 620 135 300 260 14 | Out-Null
Add-Footer $s 6

# 7. Package responsibilities
$s = New-BlankSlide $presentation $blankLayout
Add-SlideTitle $s "Vai trò các package" "Mỗi package giữ một phần trách nhiệm riêng để giảm phụ thuộc"
Add-Callout $s "model" @("Entity hierarchy", "WorldMap, Terrain, Season", "Action và dữ liệu sinh học") 60 125 260 130
Add-Callout $s "engine" @("Simulation loop", "Entity management", "Season and population") 350 125 260 130
Add-Callout $s "strategy" @("Passive, Scared", "Hunter, Aggressive", "Quyết định Action") 640 125 260 130
Add-Callout $s "view" @("GameView, Camera", "Renderer, SpriteRenderer", "Canvas và sidebar") 60 300 260 130
Add-Callout $s "sound" @("SoundManager", "Âm thanh theo sự kiện", "WAV resources") 350 300 260 130
Add-Callout $s "util" @("Constants", "Vector2D", "Dùng chung toàn project") 640 300 260 130
Add-Footer $s 7

# 8. Class diagram
$s = New-BlankSlide $presentation $blankLayout
Add-SlideTitle $s "Biểu đồ lớp" "Các lớp chính và quan hệ kế thừa/sử dụng trong hệ thống"
Add-FitPicture $s (Join-Path $root "uml\class-diagram.png") 32 98 895 390 | Out-Null
Add-Footer $s 8

# 9. Design explanation
$s = New-BlankSlide $presentation $blankLayout
Add-SlideTitle $s "Giải thích thiết kế lớp" "Cách tổ chức lớp giúp chương trình dễ mở rộng"
Add-Bullets $s @(
    "Entity chứa thông tin chung: id, tên, vị trí, trạng thái sống, độ ưu tiên và kích thước.",
    "Animal mở rộng Entity bằng máu, đói, khát, tốc độ, trạng thái và strategy hiện tại.",
    "Plant mở rộng Entity bằng logic tăng trưởng, bị ăn và lan rộng.",
    "SimulationEngine điều phối vòng lặp; EntityManager quản lý danh sách thực thể; SeasonManager quản lý mùa.",
    "Renderer là interface giúp đổi giữa BasicRenderer và SpriteRenderer mà không sửa engine."
) 70 125 820 315 17 | Out-Null
Add-Footer $s 9

# 10. OOP
$s = New-BlankSlide $presentation $blankLayout
Add-SlideTitle $s "Kỹ thuật OOP đã áp dụng" "Các nguyên lý hướng đối tượng thể hiện trực tiếp trong code"
Add-Callout $s "Đóng gói" @("Thuộc tính private/protected", "Getter/setter kiểm soát trạng thái", "Giảm truy cập trực tiếp") 50 120 250 145
Add-Callout $s "Kế thừa" @("Animal, Plant kế thừa Entity", "Các loài kế thừa Animal/Plant", "Tái sử dụng logic chung") 355 120 250 145
Add-Callout $s "Đa hình" @("Danh sách Entity", "Renderer interface", "SurvivalStrategy interface") 660 120 250 145
Add-Callout $s "Trừu tượng hóa" @("Abstract class", "Interface", "Che giấu chi tiết cài đặt") 50 310 250 145
Add-Callout $s "Strategy Pattern" @("Passive/Scared/Hunter/Aggressive", "Đổi hành vi runtime", "Dễ thêm strategy mới") 355 310 250 145
Add-Callout $s "Factory" @("AnimalReproductionFactory", "Tạo con non theo loài", "Gom logic sinh sản") 660 310 250 145
Add-Footer $s 10

# 11. Strategy
$s = New-BlankSlide $presentation $blankLayout
Add-SlideTitle $s "Strategy Pattern trong hành vi động vật" "Tách thuật toán quyết định hành vi khỏi lớp Animal"
Add-ProcessStep $s 1 "Nhận dữ liệu môi trường" "Animal gửi trạng thái bản thân, danh sách entity gần đó và WorldMap cho strategy." 80 125 360 85
Add-ProcessStep $s 2 "Quyết định hành động" "Strategy trả về Action: WANDER, MOVE_TO, EAT, DRINK, ATTACK, FLEE hoặc HIDE." 80 240 360 85
Add-ProcessStep $s 3 "Thực thi hành động" "Animal.executeAction() xử lý di chuyển, ăn, uống nước, tấn công hoặc chạy trốn." 80 355 360 85
Add-Callout $s "Lợi ích" @(
    "Không cần viết toàn bộ AI trong từng lớp động vật.",
    "Có thể đổi strategy khi runtime, ví dụ thỏ/hươu quá đói chuyển sang AggressiveStrategy.",
    "Dễ bổ sung hành vi mới mà ít ảnh hưởng code cũ."
) 520 135 360 260
Add-Footer $s 11

# 12. Algorithms
$s = New-BlankSlide $presentation $blankLayout
Add-SlideTitle $s "Thuật toán và xử lý nổi bật" "Các bước xử lý chính trong mỗi tick mô phỏng"
Add-Bullets $s @(
    "Simulation loop theo deltaTime giúp mô phỏng ổn định theo thời gian.",
    "Tìm entity gần bằng cách lọc các thực thể sống trong bán kính tầm nhìn.",
    "Di chuyển dùng vector, hệ số địa hình, gia tốc và giảm tốc khi gần mục tiêu.",
    "Va chạm mềm dùng priority để thực thể ưu tiên thấp nhường đường.",
    "Sinh sản mùa xuân dựa trên điều kiện sức khỏe, độ đói, độ khát, tuổi và xác suất."
) 70 125 820 290 18 | Out-Null
Add-Footer $s 12

# 13. UI usage
$s = New-BlankSlide $presentation $blankLayout
Add-SlideTitle $s "Giao diện và hướng dẫn sử dụng" "Các thao tác chính khi demo chương trình"
Add-FitPicture $s (Join-Path $root "images\demo-main.png") 55 125 405 270 | Out-Null
Add-Bullets $s @(
    "Bắt đầu/Tạm dừng mô phỏng.",
    "Điều chỉnh tốc độ từ 0.5x đến 8x.",
    "Đổi giữa Basic Shapes và Sprites.",
    "Click trái để chọn entity hoặc đặt đối tượng.",
    "Click phải để thêm động vật/thực vật.",
    "Scroll để zoom, kéo chuột để pan camera."
) 515 130 360 250 15 | Out-Null
Add-Footer $s 13

# 14. Testing
$s = New-BlankSlide $presentation $blankLayout
Add-SlideTitle $s "Kiểm thử" "JUnit kiểm tra các hành vi quan trọng của mô phỏng"
Add-Callout $s "HunterStrategyTest" @("Săn mồi giữ đúng target", "Di chuyển đến thức ăn gần nhất", "Kiểm tra trạng thái RUNNING") 70 130 260 220
Add-Callout $s "TigerTest" @("Sát thương phục kích trong rừng", "Thỏ đi vào bụi rậm", "Sói không vào bụi rậm") 350 130 260 220
Add-Callout $s "EntityManagerTest" @("Đổi strategy khi quá đói", "Hungry deer -> AggressiveStrategy", "Đảm bảo logic runtime") 630 130 260 220
Add-Bullets $s @(
    "Các test tập trung vào hành vi có rủi ro cao: chọn mục tiêu, tấn công, đổi strategy và ràng buộc địa hình.",
    "Nhờ test, nhóm dễ phát hiện lỗi khi thay đổi logic AI hoặc cập nhật EntityManager."
) 80 390 800 80 14 | Out-Null
Add-Footer $s 14

# 15. Limitations and future
$s = New-BlankSlide $presentation $blankLayout
Add-SlideTitle $s "Hạn chế và hướng phát triển" "Những điểm có thể cải thiện sau phiên bản hiện tại"
Add-Callout $s "Hạn chế" @(
    "Ảnh demo cần bổ sung đầy đủ sau khi chạy chương trình.",
    "Một số loài như cá và vịt có thể cần sprite riêng.",
    "Test chưa bao phủ toàn bộ vòng đời mô phỏng.",
    "Quy luật sinh thái còn có thể mô phỏng phức tạp hơn."
) 70 125 390 260
Add-Callout $s "Hướng phát triển" @(
    "Bổ sung nhiều loài động vật, thực vật và địa hình.",
    "Cải thiện giao diện thống kê theo thời gian.",
    "Thêm màn hình cấu hình trước khi bắt đầu.",
    "Mở rộng test cho WorldMap, SimulationEngine và sinh sản."
) 500 125 390 260
Add-Footer $s 15

# 16. Thank you
$s = New-BlankSlide $presentation $blankLayout
$bar = $s.Shapes.AddShape(1, 0, 0, 960, 540)
$bar.Fill.ForeColor.RGB = $HustRed
$bar.Line.Visible = 0
$thanks = Add-TextBox $s "THANK YOU!" 0 190 960 70 44 $White $true
$thanks.TextFrame.TextRange.ParagraphFormat.Alignment = 2
$sub = Add-TextBox $s "Wild-Life Eco Simulation | Lập trình hướng đối tượng" 0 270 960 40 18 $White $false
$sub.TextFrame.TextRange.ParagraphFormat.Alignment = 2

$presentation.SaveAs($out)
$presentation.Close()
$pp.Quit()

Write-Host "Created presentation:"
Write-Host $out
}
finally {
    if ($presentation) {
        try { $presentation.Close() } catch {}
    }
    if ($pp) {
        try { $pp.Quit() } catch {}
    }
}
