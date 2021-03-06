package com.maddyhome.idea.vim.ex.handler;

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

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.maddyhome.idea.vim.ex.CommandHandler;
import com.maddyhome.idea.vim.ex.CommandName;
import com.maddyhome.idea.vim.ex.ExCommand;
import com.maddyhome.idea.vim.ex.ExException;
import com.maddyhome.idea.vim.group.CommandGroups;
import com.maddyhome.idea.vim.group.MotionGroup;
import com.maddyhome.idea.vim.group.RegisterGroup;
import com.maddyhome.idea.vim.helper.EditorHelper;

/**
 *
 */
public class PutLinesHandler extends CommandHandler {
  public PutLinesHandler() {
    super(new CommandName[]{
      new CommandName("pu", "t")
    }, RANGE_OPTIONAL | ARGUMENT_REQUIRED | WRITABLE);
  }

  public boolean execute(Editor editor, DataContext context, ExCommand cmd) throws ExException {
    int line = cmd.getLine(editor, context);
    String arg = cmd.getArgument();
    boolean before = false;
    if (arg.length() > 0 && arg.charAt(0) == '!') {
      before = true;
      arg = arg.substring(1).trim();
    }
    if (arg.length() > 0) {
      if (!CommandGroups.getInstance().getRegister().selectRegister(arg.charAt(0))) {
        return false;
      }
    }
    else {
      CommandGroups.getInstance().getRegister().selectRegister(RegisterGroup.REGISTER_DEFAULT);
    }

    MotionGroup.moveCaret(editor, context, EditorHelper.getLineStartOffset(editor, line));
    if (before) {
      return CommandGroups.getInstance().getCopy().putTextBeforeCursor(editor, context, 1, true, false);
    }
    else {
      return CommandGroups.getInstance().getCopy().putTextAfterCursor(editor, context, 1, true, false);
    }
  }
}
