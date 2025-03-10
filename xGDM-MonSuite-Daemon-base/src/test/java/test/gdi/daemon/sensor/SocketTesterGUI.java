/*-
 * #%L
 * xGDM-MonSuite Daemon (Base)
 * %%
 * Copyright (C) 2022 - 2025 grit GmbH
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package test.gdi.daemon.sensor;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.TextField;

public class SocketTesterGUI extends Frame {
    private static final long serialVersionUID = -2475319476662737747L;

    private GridLayout gridLayout1 = new GridLayout();

    private TextField textField1 = new TextField();

    public SocketTesterGUI() {
        try {
            jbInit();
        } catch ( Exception e ) {
            e.printStackTrace();
        }

    }

    /**
     * 
     * @param args
     */
    public static void main( String[] args ) {
        new SocketTesterGUI();
    }

    private void jbInit()
                            throws Exception {
        this.setLayout( gridLayout1 );
        gridLayout1.setColumns( 4 );
        gridLayout1.setRows( 5 );
        textField1.setText( "textField1" );
        this.add( textField1, null );
    }
}