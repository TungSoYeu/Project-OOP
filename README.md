# Wild-Life Eco Simulation

Du an JavaFX mo phong he sinh thai hoang da theo huong OOP + Strategy Pattern.

## Yeu cau

- Java 21 tro len. May hien tai da kiem tra voi Oracle JDK 21.
- Khong can cai Maven rieng neu dung `mvnw.cmd`.

## Chay nhanh

```powershell
.\run.bat
```

Hoac chay truc tiep bang Maven Wrapper:

```powershell
.\mvnw.cmd clean javafx:run
```

## Build va test

```powershell
.\mvnw.cmd package
.\mvnw.cmd test
```

File build nam o thu muc `target/`.

## Dieu khien

- `Bat dau/Tam dung`: chay hoac dung mo phong.
- `Toc do`: doi toc do simulation tu 0.5x den 8x.
- `Do hoa`: doi giua Basic Shapes va Sprites.
- `Xem vung`: toan ban do, dong co, rung ram, ho nuoc.
- Click trai voi cong cu `Chon`, `Gieo co`, `Dat da`.
- Click phai de them Tho, Huou, Soi, Ho, Voi, Tho san.
- Scroll de zoom; keo chuot giua hoac keo khi o tool `Chon` de pan camera.

## Tinh nang chinh

- Ban do gom dong co, rung ram, ho nuoc, bun, bui ram va vach da.
- BioLogic tach khoi ViewLogic: entity/strategy nam trong `model` va `strategy`, renderer nam trong `view`.
- PassiveStrategy, HunterStrategy, ScaredStrategy, AggressiveStrategy co the doi runtime.
- Dong vat khat tu tim nuoc, dung khi gap vat can, va nho uu tien khi va cham.
- Soi chay nhanh khi duoi moi; tho co the chui vao bui ram noi soi khong vao duoc.
- Thuc vat sinh soi theo chu ky va bi anh huong boi mua.
- Am thanh duoc hook theo su kien: chim hot, ho gam, uong nuoc, buoc chan tren la.
