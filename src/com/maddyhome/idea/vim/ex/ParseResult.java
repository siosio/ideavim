package com.maddyhome.idea.vim.ex;

/*
 * IdeaVim - A Vim emulator plugin for IntelliJ Idea
 * Copyright (C) 2003-2005 Rick Maddy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/**
 *
 */
public class ParseResult {
  public ParseResult(Ranges ranges, String command, String argument) {
    this.ranges = ranges;
    this.argument = argument;
    this.command = command;
  }

  public String getCommand() {
    return command;
  }

  public String getArgument() {
    return argument;
  }

  public Ranges getRanges() {
    return ranges;
  }

  protected Ranges ranges;
  protected String command;
  protected String argument;
}
