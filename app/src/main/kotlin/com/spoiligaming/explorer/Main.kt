/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2025 SpoilerRules
 *
 * Server List Explorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Server List Explorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Server List Explorer.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.spoiligaming.explorer

import com.spoiligaming.explorer.ui.launchInterface

fun main(args: Array<String>) {
    val env = if (System.getProperty("env") == "dev") "dev" else "prod"
    System.setProperty("log4j2.configurationFile", "log4j2-$env.xml")
    System.setProperty("app.logs.dir", LogStorage.logsDir.absolutePath)

    ArgsParser.parse(args)

    launchInterface()
}
