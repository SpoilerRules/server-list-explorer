rem This script was generated with assistance from ChatGPT-4o by OpenAI.

@echo off
setlocal EnableDelayedExpansion

set "header_kt_0=/*"
set "header_kt_1= * This file is part of Server List Explorer."
set "header_kt_2= * Copyright (C) 2025 SpoilerRules"
set "header_kt_3= *"
set "header_kt_4= * Server List Explorer is free software: you can redistribute it and/or modify"
set "header_kt_5= * it under the terms of the GNU General Public License as published by"
set "header_kt_6= * the Free Software Foundation, either version 3 of the License, or"
set "header_kt_7= * (at your option) any later version."
set "header_kt_8= *"
set "header_kt_9= * Server List Explorer is distributed in the hope that it will be useful,"
set "header_kt_10= * but WITHOUT ANY WARRANTY; without even the implied warranty of"
set "header_kt_11= * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the"
set "header_kt_12= * GNU General Public License for more details."
set "header_kt_13= *"
set "header_kt_14= * You should have received a copy of the GNU General Public License"
set "header_kt_15= * along with Server List Explorer.  If not, see <https://www.gnu.org/licenses/>."
set "header_kt_16=*/"

set "header_prop_0=# This file is part of Server List Explorer."
set "header_prop_1=# Copyright (C) 2025 SpoilerRules"
set "header_prop_2=#"
set "header_prop_3=# Server List Explorer is free software: you can redistribute it and/or modify"
set "header_prop_4=# it under the terms of the GNU General Public License as published by"
set "header_prop_5=# the Free Software Foundation, either version 3 of the License, or"
set "header_prop_6=# (at your option) any later version."
set "header_prop_7=#"
set "header_prop_8=# Server List Explorer is distributed in the hope that it will be useful,"
set "header_prop_9=# but WITHOUT ANY WARRANTY; without even the implied warranty of"
set "header_prop_10=# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the"
set "header_prop_11=# GNU General Public License for more details."
set "header_prop_12=#"
set "header_prop_13=# You should have received a copy of the GNU General Public License"
set "header_prop_14=# along with Server List Explorer.  If not, see <https://www.gnu.org/licenses/>."

for /R %%F in (*.kt *.kts) do (
    findstr /C:"* This file is part of Server List Explorer." "%%F" >nul
    if errorlevel 1 (
        echo Adding header to %%F
        >"%%F.temp" (
            for /L %%i in (0,1,16) do echo !header_kt_%%i!
            echo(
            type "%%F"
        )
        move /Y "%%F.temp" "%%F" >nul
    )
)

for /R %%F in (*.properties) do (
    findstr /C:"# This file is part of Server List Explorer." "%%F" >nul
    if errorlevel 1 (
        echo Adding header to %%F
        >"%%F.temp" (
            for /L %%i in (0,1,14) do echo !header_prop_%%i!
            echo(
            type "%%F"
        )
        move /Y "%%F.temp" "%%F" >nul
    )
)

echo.
echo Done.
pause
