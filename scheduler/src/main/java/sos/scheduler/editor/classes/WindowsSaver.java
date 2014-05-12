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
package sos.scheduler.editor.classes;

import java.awt.Point;
import java.util.prefs.Preferences;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

/**
* \class WindowsSaver 
* 
* \brief WindowsSaver - 
* 
* \details
*
* \code
*   .... code goes here ...
* \endcode
*
* <p style="text-align:center">
* <br />---------------------------------------------------------------------------
* <br /> APL/Software GmbH - Berlin
* <br />##### generated by ClaviusXPress (http://www.sos-berlin.com) #########
* <br />---------------------------------------------------------------------------
* </p>
* \author Uwe Risse
* \version 08.11.2011
* \see reference
*
* Created on 08.11.2011 15:29:14
 */

public class WindowsSaver {

	@SuppressWarnings("unused")
	private final String	conClassName	= "WindowsSaver";
	private Shell			shell;
	private Preferences		prefs;
	private Point			defaultSize;
	private Point			defaultLocation;
	private String			className;

	public WindowsSaver(Class c, Shell s, int x, int y) {
		this.prefs = Preferences.userNodeForPackage(c);
		this.shell = s;
		className = c.getName();
		defaultSize = new Point(x, y);
		defaultLocation = new Point(100, 100);

	}

	private int getInt(String s, int def) {
		try {
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e) {
			return def;
		}
	}

	public void restoreWindow() {
		shell.setSize(getInt(prefs.get("win:sizeX:" + className, String.valueOf(defaultSize.x)), defaultSize.x),
				getInt(prefs.get("win:sizeY:" + className, String.valueOf(defaultSize.y)), defaultSize.y));
		shell.setLocation(getInt(prefs.get("win:locateX:" + className, String.valueOf(defaultLocation.x)), defaultLocation.x),
				getInt(prefs.get("win:locateY:" + className, String.valueOf(defaultLocation.y)), defaultLocation.y));
	}

	public void restoreWindowLocation() {
		shell.setLocation(getInt(prefs.get("win:locateX:" + className, String.valueOf(defaultLocation.x)), defaultLocation.x),
				getInt(prefs.get("win:locateY:" + className, String.valueOf(defaultLocation.y)), defaultLocation.y));
	}

	public void restoreWindowSize() {
		shell.setSize(getInt(prefs.get("win:sizeX:" + className, String.valueOf(defaultSize.x)), defaultSize.x),
				getInt(prefs.get("win:sizeY:" + className, String.valueOf(defaultSize.y)), defaultSize.y));
	}

	public void saveWindow() {
		prefs.put("win:sizeX:" + className, String.valueOf(shell.getSize().x));
		prefs.put("win:sizeY:" + className, String.valueOf(shell.getSize().y));
		prefs.put("win:locateX:" + className, String.valueOf(shell.getLocation().x));
		prefs.put("win:locateY:" + className, String.valueOf(shell.getLocation().y));
	}

	public void saveTableColumn(String tableName, TableColumn t) {
		String name = "_" + t.getText();
		prefs.node(tableName + "/col/" + name).put("width", String.valueOf(t.getWidth()));
	}

	public void restoreTableColumn(String tableName, TableColumn t, int def) {
		String name = "_" + t.getText();
		try {
			t.setWidth(this.getInt(prefs.node(tableName + "/col/" + name).get("width", String.valueOf(def)), def));
		}
		catch (Exception e) {
			t.setWidth(def);
		}
	}

}