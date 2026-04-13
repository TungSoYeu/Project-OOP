# Wild-Life Eco Simulation

Du an JavaFX mo phong he sinh thai hoang da theo huong OOP + Strategy Pattern.

## Yeu cau

- Java 21 tro len
- Maven 3.9+

## Cach chay

Neu may da co Maven trong `PATH`:

```powershell
mvn clean javafx:run
```

Neu chua co Maven trong `PATH`, du an da kem file `maven.zip`. Giai nen va chay:

```powershell
Expand-Archive -LiteralPath .\maven.zip -DestinationPath .\.mvn-install -Force
.\.mvn-install\apache-maven-3.9.6\bin\mvn.cmd clean javafx:run
```

## Build jar

```powershell
.\.mvn-install\apache-maven-3.9.6\bin\mvn.cmd clean package
```

File build nam o thu muc `target/`.

## Chay test

```powershell
.\.mvn-install\apache-maven-3.9.6\bin\mvn.cmd test
```

## Tinh nang chinh

- Ban do gom dong co, rung ram, ho nuoc va vat can.
- Dong vat hanh dong theo chien luoc sinh ton.
- Ho tro spawn entity, gieo co, dat da, zoom/pan camera.
- He thong mua anh huong den toc do phat trien cua thuc vat.
