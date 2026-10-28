$ErrorActionPreference = 'Stop'
$project = Split-Path $PSScriptRoot -Parent
$output = Join-Path $project 'build/reference-tests'
New-Item -ItemType Directory -Force $output | Out-Null
& "$env:JAVA_HOME/bin/javac.exe" -encoding UTF-8 -d $output `
    (Join-Path $project 'mod/common/src/main/java/io/github/fishstiz/packed_packs/util/FolderPackIds.java') `
    (Join-Path $PSScriptRoot 'FolderPackIdsTest.java')
if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed' }
& "$env:JAVA_HOME/bin/java.exe" -cp $output FolderPackIdsTest
if ($LASTEXITCODE -ne 0) { throw 'Reference tests failed' }
