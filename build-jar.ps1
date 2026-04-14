# Build JAR file for ToDo App
Write-Host "Building JAR file..." -ForegroundColor Green

# Clean and create bin directory
if (Test-Path bin) { Remove-Item -Recurse -Force bin }
New-Item -ItemType Directory -Path bin | Out-Null

# Compile
Write-Host "Compiling Java sources..."
javac -d bin -sourcepath src src/com/todoapp/Main.java

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Compilation successful!" -ForegroundColor Green
    
    # Create JAR
    Write-Host "Creating JAR file..."
    jar cfe ToDoApp.jar com.todoapp.Main -C bin .
    
    if (Test-Path ToDoApp.jar) {
        Write-Host "✓ JAR created: ToDoApp.jar" -ForegroundColor Green
        Write-Host "" 
        Write-Host "Download Launch4j: https://sourceforge.net/projects/launch4j/" -ForegroundColor Cyan
    } else {
        Write-Host "✗ Compilation failed!" -ForegroundColor Red
    }
}
