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
package de.grit.gdi.daemon.utils;

public class ImageAnalyserResult {
    private String message;

    private int width;

    private int height;

    private String imgtype;

    private boolean result;

    public ImageAnalyserResult() {
        message = null;
        width = -1;
        height = -1;
        imgtype = null;
    }

    public void setMessage( String message ) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setWidth( int width ) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public void setHeight( int height ) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public void setImgtype( String imgtype ) {
        this.imgtype = imgtype;
    }

    public String getImgtype() {
        return imgtype;
    }

    public void setResult( boolean result ) {
        this.result = result;
    }

    public boolean isResult() {
        return result;
    }
}