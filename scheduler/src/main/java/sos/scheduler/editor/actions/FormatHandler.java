/**
 * Copyright (C) 2014 BigLoupe http://bigloupe.github.io/SoS-JobScheduler/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
/********************************************************* begin of preamble
**
** Copyright (C) 2003-2012 Software- und Organisations-Service GmbH. 
** All rights reserved.
**
** This file may be used under the terms of either the 
**
**   GNU General Public License version 2.0 (GPL)
**
**   as published by the Free Software Foundation
**   http://www.gnu.org/licenses/gpl-2.0.txt and appearing in the file
**   LICENSE.GPL included in the packaging of this file. 
**
** or the
**  
**   Agreement for Purchase and Licensing
**
**   as offered by Software- und Organisations-Service GmbH
**   in the respective terms of supply that ship with this file.
**
** THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
** IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
** THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
** PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
** BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
** CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
** SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
** INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
** CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
** ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
** POSSIBILITY OF SUCH DAMAGE.
********************************************************** end of preamble*/
package sos.scheduler.editor.actions;

import java.util.Iterator;
import java.util.List;

import org.jdom.Namespace;
import org.jdom.ProcessingInstruction;
import org.jdom.filter.ContentFilter;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FormatHandler extends DefaultHandler implements ContentHandler {
    private static final String[] _noLinebreaks = { "code", "strong" };

    private String                _encoding     = "utf-8";

    private ActionsDom      _dom          = null;

    private StringBuffer          _sb           = new StringBuffer();

    private StringBuffer          _text         = new StringBuffer();

    private int                   _level        = 0;

    private boolean               _isOpen       = false;

    private String                _indentStr    = "    ";

    private String                _indent       = "    ";

    private int                   _xhtml        = -1;

    private String                _root         = null;

    private String                _ns           = null;

    private String                _xhtmlNs      = null;

    private boolean               _doBreak      = true;
 

    public FormatHandler(ActionsDom dom) {
        _dom = dom;
        _root = _dom.getRoot().getName();
        _ns = _dom.getNamespace().getURI();
        _xhtmlNs = _dom.getNamespace("xhtml").getURI();
    }


    public void setEnconding(String encoding) {
        _encoding = encoding;
    }


    public String getXML() {
        return _sb.toString();
    }


    public void startDocument() {
        _sb.append("<?xml version=\"1.0\" encoding=\"" + _encoding + "\"?>\n");

        List pis = _dom.getDoc().getContent(new ContentFilter(ContentFilter.PI));
        for (Iterator it = pis.iterator(); it.hasNext();) {
            ProcessingInstruction pi = (ProcessingInstruction) it.next();
            _sb.append("<?" + pi.getTarget() + " " + pi.getValue() + "?>\n");
        }
    }


    public void characters(char[] ch, int start, int length) throws SAXException {
        _text.append(new String(ch, start, length));
    }


    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        _doBreak = doLinebreak(qName);
        _level--;
        _indent = strRepeat(_indentStr, _level);

        if (_xhtml == _level)
            _xhtml = -1;

        String text = _text.toString().trim();
        _text = new StringBuffer();

        boolean hasText = text.length() > 0;

        if (_isOpen && !hasText)
            _sb.append("/>" + (_doBreak ? "\n" + _indent : ""));
        // _sb.append("/>\n");
        else if (_isOpen)
            _sb.append(">" + (_doBreak ? "\n" + _indent : ""));
        // _sb.append(">\n");

        if (hasText) {
            _sb.append(formatText(text) + (_doBreak ? "\n" : ""));
        }

        if (!_isOpen)
            // _sb.append("</" + qName + ">" + (_doBreak ? "\n"+_indent : ""));
            _sb.append("</" + qName + ">" + (_doBreak ? "\n" + _indent : ""));
        else if (hasText)
            _sb.append((_doBreak ? _indent : "") + "</" + qName + ">"
                    + (_doBreak ? "\n" + strRepeat(_indentStr, _level - 1) : ""));

        if (qName.equalsIgnoreCase("div"))
            _xhtml--;

        _isOpen = false;
    }


    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        _indent = strRepeat(_indentStr, _level);

        if (_isOpen) {
            _sb.append(">");
            _sb.append(breakline(_doBreak) + _indent);
        }

        _doBreak = doLinebreak(qName);

        StringBuffer attributes = new StringBuffer();
        String sep = " ";
        String sepStr = "\n" + _indent + strRepeat(" ", new String("<" + qName).length() + 1);

        // iterate atributes
        for (int i = 0; i < atts.getLength(); i++) {
            String name = atts.getQName(i);
            String value = atts.getValue(i);
            String uri = atts.getURI(i);

            // add attribute
            if (!uri.equals("")) {
                attributes.append(sep);
                attributes.append("xmlns:xsi" + "=\"" + uri + "\"");
                sep = sepStr;
            }
            attributes.append(sep);
            attributes.append(name + "=\"" + value + "\"");
            sep = sepStr;
        }

        // add namespace
        if (namespaceURI != null && !namespaceURI.equals("")) {
            // only first start of xhtml
            if (_xhtml <= 0 && namespaceURI.equals(_xhtmlNs)) {
                _xhtml = _level;
                attributes.append(sep);
                attributes.append("xmlns" + "=\"" + namespaceURI + "\"");
            }
            // always other namespaces
            else if (!namespaceURI.equals(_ns) && !namespaceURI.equals(_xhtmlNs)) {
                attributes.append(sep);
                attributes.append("xmlns" + "=\"" + namespaceURI + "\"");
            }
        }

        // declare root element
        if (qName.equals(_root)) {
            // main namespace
            if (_ns != null && !_ns.equals("")) {
                attributes.append(sep);
                attributes.append("xmlns" + "=\"" + _ns + "\"");
            }

            // additinal namespaces
            for (Iterator it = _dom.getAdditinalNamespaces().iterator(); it.hasNext();) {
                Namespace ns = (Namespace) it.next();
                if (!ns.getPrefix().equals("xsi")) {
                    attributes.append(sep);
                    attributes.append("xmlns:" + ns.getPrefix() + "=\"" + ns.getURI() + "\"");
                }
            }
        }

        // _sb.append((doBreak ? _indent : "") + "<" + qName +
        // attributes.toString());
        _sb.append("<" + qName + attributes.toString());

        _isOpen = true;
        _level++;

    }


    private String strRepeat(String str, int cnt) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < cnt; i++)
            sb.append(str);
        return sb.toString();
    }


    private String breakline(boolean doBreak) {
        if (doBreak) {
            switch (_level) {
                case 0:
                    return strRepeat("\n", 1);
                case 1:
                    return strRepeat("\n", 2);
                case 2:
                    return strRepeat("\n", 1);
                default:
                    return "\n";
            }
        } else
            return "";
    }


    private boolean doLinebreak(String name) {
        for (int i = 0; i < _noLinebreaks.length; i++)
            if (_noLinebreaks[i].equals(name))
                return false;
        return true;
    }


    private StringBuffer formatText(String text) {
        StringBuffer sb = new StringBuffer(_indentStr);

        text = text.replaceAll("\\s{1,}", " ").trim();
        // String replacement = "\n" + _indent + _indentStr;
        // text = text.replaceAll("\n", replacement);

        sb.append(text);
        // sb.append("\n");
        return sb;
    }
}
