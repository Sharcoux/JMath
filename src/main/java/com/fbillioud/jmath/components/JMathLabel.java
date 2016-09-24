/* 
 * Copyright 2016 François Billioud.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fbillioud.jmath.components;

import com.fbillioud.jmath.MathComponent;
import java.awt.Font;
import java.awt.font.LineMetrics;
import javax.swing.JLabel;

/**
 *
 * @author François Billioud
 */
public class JMathLabel extends JLabel implements MathComponent {

    public JMathLabel(String text) {
        super(text);
        setOpaque(false);
    }
    
    @Override
    public float getFontSize() {return getFont().getSize();}
    @Override
    public void setFontSize(float size) { setFont(getFont().deriveFont(size)); }
    
    @Override
    public void setFont(Font font) {
        super.setFont(isItalic() ? font.deriveFont(Font.ITALIC) : font);
    }

    @Override
    public float getAlignmentY() {
        LineMetrics fm = getFontMetrics(getFont()).getLineMetrics(getText(), getGraphics());
        return (fm.getAscent())/fm.getHeight();
    }
    
    public boolean isItalic() {return getFont()==null ? false : getFont().isItalic();}
    public void setItalic(boolean b) {setFont(getFont().deriveFont(b ? Font.ITALIC : Font.PLAIN));}
    
}
