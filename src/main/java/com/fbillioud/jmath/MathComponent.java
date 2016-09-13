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
package com.fbillioud.jmath;

import java.util.List;
import javax.swing.JComponent;
import org.jsoup.nodes.Element;

/**
 * A JComponent that can be represented inside another MathComponent.
 * A MathComponent can contain other MathComponents.
 * @author François Billioud
 */
public interface MathComponent {
    /**
     * Get the current font size of this JComponent
     * @return the font size as an integer
     */
    public int getFontSize();
    /**
     * Set the font size to use in this JComponent
     * @param size the font size to use, as an integer
     */
    public void setFontSize(int size);
    /**
     * Get all children {@link MathComponent}
     * @return the list of all children
     */
    public List<MathComponent> getMathComponents();
    /**
     * Returns the JComponent that reprensents this MathComponent.
     * Should return itself.
     * @return itself, or the JComponent representing this MathComponent
     */
    public JComponent asComponent();
    /**
     * Get the parent of this MathComponent.
     * @return this component's parent, or null
     */
    public MathComponent getParentComponent();
    
    /**
     * Error thrown while parsing MathML.
     */
    public static class MathMLParsingException extends Exception {
        public MathMLParsingException(String message, Element mathML) {
            super(message+"\n"+mathML.outerHtml());
        }
    }
}
