package com.maddyhome.idea.vim.helper;

/*
 * IdeaVim - A Vim emulator plugin for IntelliJ Idea
 * Copyright (C) 2003-2004 Rick Maddy
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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.maddyhome.idea.vim.group.CommandGroups;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

public class DigraphSequence
{
    public DigraphSequence()
    {
    }

    public static boolean isDigraphStart(KeyStroke key)
    {
        if ((key.getModifiers() & KeyEvent.CTRL_MASK) != 0)
        {
            if (key.getKeyCode() == KeyEvent.VK_K || key.getKeyCode() == KeyEvent.VK_V)
            {
                return true;
            }
        }

        return false;
    }

    public DigraphResult processKey(KeyStroke key, Editor editor, DataContext context)
    {
        switch (digraphState)
        {
            case DIG_STATE_START:
                logger.debug("DIG_STATE_START");
                if (key.getKeyCode() == KeyEvent.VK_K && (key.getModifiers() & KeyEvent.CTRL_MASK) != 0)
                {
                    logger.debug("found Ctrl-K");
                    digraphState = DIG_STATE_DIG_ONE;
                    return DigraphResult.OK;
                }
                else if (key.getKeyCode() == KeyEvent.VK_V && (key.getModifiers() & KeyEvent.CTRL_MASK) != 0)
                {
                    logger.debug("found Ctrl-V");
                    digraphState = DIG_STATE_CODE_START;
                    codeChars = new char[8];
                    codeCnt = 0;
                    return DigraphResult.OK;
                }
                else
                {
                    return new DigraphResult(key);
                }
            case DIG_STATE_DIG_ONE:
                logger.debug("DIG_STATE_DIG_ONE");
                if (key.getKeyChar() != KeyEvent.CHAR_UNDEFINED)
                {
                    digraphChar = key.getKeyChar();
                    digraphState = DIG_STATE_DIG_TWO;

                    return DigraphResult.OK;
                }
                else
                {
                    digraphState = DIG_STATE_START;
                    return DigraphResult.BAD;
                }
            case DIG_STATE_DIG_TWO:
                logger.debug("DIG_STATE_DIG_TWO");
                digraphState = DIG_STATE_START;
                if (key.getKeyChar() != KeyEvent.CHAR_UNDEFINED)
                {
                    char ch = CommandGroups.getInstance().getDigraph().getDigraph(digraphChar, key.getKeyChar());

                    return new DigraphResult(KeyStroke.getKeyStroke(ch));
                }

                return DigraphResult.BAD;
            case DIG_STATE_CODE_START:
                logger.debug("DIG_STATE_CODE_START");
                switch (key.getKeyChar())
                {
                    case 'o':
                    case 'O':
                        codeMax = 3;
                        digraphState = DIG_STATE_CODE_CHAR;
                        codeType = 8;
                        logger.debug("Octal");
                        return DigraphResult.OK;
                    case 'x':
                    case 'X':
                        codeMax = 2;
                        digraphState = DIG_STATE_CODE_CHAR;
                        codeType = 16;
                        logger.debug("hex2");
                        return DigraphResult.OK;
                    case 'u':
                        codeMax = 4;
                        digraphState = DIG_STATE_CODE_CHAR;
                        codeType = 16;
                        logger.debug("hex4");
                        return DigraphResult.OK;
                    case 'U':
                        codeMax = 8;
                        digraphState = DIG_STATE_CODE_CHAR;
                        codeType = 16;
                        logger.debug("hex8");
                        return DigraphResult.OK;
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        codeMax = 3;
                        digraphState = DIG_STATE_CODE_CHAR;
                        codeType = 10;
                        codeChars[codeCnt++] = key.getKeyChar();
                        logger.debug("decimal");
                        return DigraphResult.OK;
                    default:
                        switch (key.getKeyCode())
                        {
                            case KeyEvent.VK_TAB:
                                KeyStroke code = KeyStroke.getKeyStroke('\t');
                                digraphState = DIG_STATE_START;

                                return new DigraphResult(code);
                            default:
                                logger.debug("unknown");
                                digraphState = DIG_STATE_START;

                                return new DigraphResult(key);
                        }
                }
            case DIG_STATE_CODE_CHAR:
                logger.debug("DIG_STATE_CODE_CHAR");
                boolean valid = false;
                switch (codeType)
                {
                    case 10:
                        if (key.getKeyChar() >= '0' && key.getKeyChar() <= '9')
                        {
                            valid = true;
                        }
                        break;
                    case 8:
                        if (key.getKeyChar() >= '0' && key.getKeyChar() <= '7')
                        {
                            valid = true;
                        }
                        break;
                    case 16:
                        if (key.getKeyChar() >= '0' && key.getKeyChar() <= '9' ||
                            key.getKeyChar() >= 'a' && key.getKeyChar() <= 'f' ||
                            key.getKeyChar() >= 'A' && key.getKeyChar() <= 'F')
                        {
                            valid = true;
                        }
                        break;
                }
                if (valid)
                {
                    logger.debug("valid");
                    codeChars[codeCnt++] = key.getKeyChar();
                    if (codeCnt == codeMax)
                    {
                        String digits = new String(codeChars, 0, codeCnt);
                        int val = Integer.parseInt(digits, codeType);
                        KeyStroke code = KeyStroke.getKeyStroke((char)val);
                        digraphState = DIG_STATE_START;

                        return new DigraphResult(code);
                    }
                    else
                    {
                        return DigraphResult.OK;
                    }
                }
                else if (codeCnt > 0)
                {
                    logger.debug("invalid");
                    String digits = new String(codeChars, 0, codeCnt);
                    int val = Integer.parseInt(digits, codeType);
                    digraphState = DIG_STATE_START;
                    KeyStroke code = KeyStroke.getKeyStroke((char)val);

                    CommandGroups.getInstance().getMacro().postKey(key, editor);

                    return new DigraphResult(code);
                }
                else
                {
                    return DigraphResult.BAD;
                }
            default:
                return DigraphResult.BAD;
        }
    }

    public static class DigraphResult
    {
        public static final int RES_OK = 0;
        public static final int RES_BAD = 1;
        public static final int RES_DONE = 2;

        public static final DigraphResult OK = new DigraphResult(RES_OK);
        public static final DigraphResult BAD = new DigraphResult(RES_BAD);

        DigraphResult(int result)
        {
            this.result = result;
            stroke = null;
        }

        DigraphResult(KeyStroke stroke)
        {
            result = RES_DONE;
            this.stroke = stroke;
        }

        public KeyStroke getStroke()
        {
            return stroke;
        }

        public int getResult()
        {
            return result;
        }

        private int result;
        private KeyStroke stroke;
    }

    private int digraphState = DIG_STATE_START;
    private char digraphChar;
    private char[] codeChars;
    private int codeCnt;
    private int codeType;
    private int codeMax;

    private static final int DIG_STATE_START = 1;
    private static final int DIG_STATE_DIG_ONE = 2;
    private static final int DIG_STATE_DIG_TWO = 3;
    private static final int DIG_STATE_CODE_START = 10;
    private static final int DIG_STATE_CODE_CHAR = 11;

    private static Logger logger = Logger.getInstance(DigraphSequence.class.getName());
}