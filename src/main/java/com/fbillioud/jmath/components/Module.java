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

import com.fbillioud.jmath.DrawShape;
import com.fbillioud.jmath.JsoupTools;
import com.fbillioud.jmath.MathComponent;
import com.fbillioud.jmath.MathComponent.MathMLParsingException;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.util.LinkedList;
import javax.swing.JComponent;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

/**
 * A Module handles a specific MathML instruction and applies it
 * to its children.
 * @author François Billioud
 */
public abstract class Module {
    /** The Jsoup Element handled by this {@link MathModule} **/
    protected final Element mathElement;
    /** The JComponent that will draw the element **/
    protected final JComponent support;
    /** The list of all children {@link MathComponent} **/
    protected final LinkedList<JComponent> mathComponents = new LinkedList<>();
    /** The layout that is in charge of positionning the children **/
    protected final MathLayout layout;

    /**
     * Represent this Jsoup MathML Element on this support using this layout.
     * @param mathElement The Jsoup MathML Element to represent
     * @param support The JComponent where the Element will be displayed
     * @param layout The LayoutManager in charge of the children position
     */
    public Module(Element mathElement, JComponent support, MathLayout layout) {
        this.mathElement = mathElement;
        this.support = support;
        this.layout = layout;
        support.setLayout(layout);
    }
    
    /** Draw the lines needed to represent the math element **/
    public void paintLines(Graphics2D g) {
        if(layout!=null) {
            layout.paintLines((Graphics2D) g.create(), support);
        }
    }
    
    /** Get the layout in charge of displaying the children. **/
    public LayoutManager getLayout() {
        return support.getLayout();
    }
    
    /** Set the child by its name **/
    public void setComponent(JComponent comp, String name) {
        support.add(comp, name);
        support.invalidate();
        comp.setName(name);
    }

    /** Set the layout that position the children **/
    public void setLayout(LayoutManager layout) {
        support.setLayout(layout);
    }

    /** Set the Y alignment of the component **/
    public void setAlignmentY(float yAlignment) {
        support.setAlignmentY(yAlignment);
    }
    
    public static abstract class MultipleChildrenModule extends Module {
        public MultipleChildrenModule(Element element, JComponent parent, MathLayout layout) {
            super(element, parent, layout);
            int name = 0;
            boolean space = false;          //at least one space has been encountered since last meaningful object
            boolean meaningful = false;     //at least one meaningful object has been encountered
            for(Node node : element.childNodes()) {
                if(node instanceof TextNode) {
                    String content = ((TextNode)node).text().trim();
                    boolean spacing = content.isEmpty();
                    if(spacing) {space = true; continue;}
                    else if(meaningful && space) {content+=" "; space = false;}
                    JMathLabel text = new JMathLabel(content);
                    if(element.nodeName().equals("mi")) {text.setItalic(true);}
                    text.setForeground(JsoupTools.getColor(element));
                    setComponent(text, ""+name++);
                    meaningful = true;
                } else if(node instanceof Element) {
                    if(space && meaningful) {
                        JMathLabel text = new JMathLabel(" ");
                        setComponent(text, ""+name++);
                        space = false;
                    }
                    Element e = (Element) node;
                    JMathDisplayer newElement;
                    if(JMathDisplayer.isModuleAvailable(e)) {
                        newElement = new JMathDisplayer(e,parent);
                    } else {
                        newElement = new JMathDisplayer(e.tagName("mrow"),parent);
                    }
                    newElement.setForeground(JsoupTools.getColor(e));
                    setComponent(newElement, ""+name++);
                    meaningful = true;
                }
            }
            //Case of an empty node
            if(space && !meaningful) {
                JMathLabel text = new JMathLabel(" ");
                setComponent(text, ""+name++);
            }
        }
    }
    /** Handle a basic row: <mrow>x</mrow> **/
    public static class ModuleRow extends MultipleChildrenModule {
        public ModuleRow(Element rowElement, JComponent parent) {
            super(rowElement, parent, new MathLayout.RowLayout());
        }
    }
    /** Handle a basic menclose tags: <menclose notation="x">x</menclose> **/
    public static class ModuleEnclose extends MultipleChildrenModule {
        public ModuleEnclose(Element encloseElement, JComponent parent, String notation) {
            super(encloseElement, parent, new MathLayout.EncloseLayout(notation));
        }
    }
    
    /** Handle Fraction: <mfrac><mrow>x</mrow><mrow>y</mrow></mfrac> **/
    public static class ModuleFraction extends Module {
        public ModuleFraction(Element fracElement, JComponent parent) {
            super(fracElement, parent, new MathLayout.FracLayout() );
            Element numElement = fracElement.child(0);
            Element denElement = fracElement.child(1);

            JMathDisplayer numerator = new JMathDisplayer(numElement, parent);
            JMathDisplayer denominator = new JMathDisplayer(denElement, parent);
            numerator.setScaleRatio(0.8f);
            numerator.setFontSize(numerator.getFontSize());
            denominator.setScaleRatio(0.8f);
            denominator.setFontSize(numerator.getFontSize());
            setComponent(numerator, "numerator");
            setComponent(denominator, "denominator");
        }
    }
    
    /** Handle a square root: <msqrt>x</msqrt> **/
    public static class ModuleSqrt extends Module {
        public ModuleSqrt(Element mathElement, JComponent parent) {
            super(mathElement, parent, new MathLayout.SQRTLayout());
            //si le contenu de la racine est en vrac, on le wrap dans un mrow. On fait de même si le childNode est un textNode. Sinon, erreur !
            Element innerSqrt = (mathElement.childNodeSize()==1&&mathElement.children().size()==1) ? mathElement.child(0) : JsoupTools.parse("<mrow></mrow>").select("mrow").first().html(mathElement.html());
            JMathDisplayer innerPane = new JMathDisplayer(innerSqrt, parent);
            setComponent(innerPane, "main");
        }
    }
    
    /** Handle a root: <msqrt><mi>x</mi><mn>3</mn></msqrt> **/
    public static class ModuleRoot extends Module {
        
        public ModuleRoot(Element mathElement, JComponent parent) {
            super(mathElement, parent, new MathLayout.RootLayout());
            Element rootedPane = mathElement.child(0);
            Element rootValue = mathElement.child(1);
            JMathDisplayer innerPane = new JMathDisplayer(rootedPane, parent);
            JMathDisplayer root = new JMathDisplayer(rootValue, parent);
            root.setScaleRatio(0.6f);
            root.setFontSize(innerPane.getFontSize());
            setComponent(innerPane, "main");
            setComponent(root, "root");
        }
    }
    
    /** Handle a fence: <mfenced>x</mfenced> **/
    public static class ModuleFenced extends Module {
        
        public ModuleFenced(Element mathElement, JComponent parent) {
            super(mathElement, parent, new MathLayout.FencedLayout());
            //Check for more specific instructions
            if(mathElement.hasAttr("open")) {
                MathLayout.FencedLayout fencedLayout = (MathLayout.FencedLayout) getLayout();
                fencedLayout.setBracket(mathElement.attr("open").trim().charAt(0),true);
            }
            if(mathElement.hasAttr("close")) {
                MathLayout.FencedLayout fencedLayout = (MathLayout.FencedLayout) getLayout();
                fencedLayout.setBracket(mathElement.attr("close").trim().charAt(0),false);
            }
            //si le contenu de la fenced est en vrac, on le wrap dans un mrow. On fait de même si le childNode est un textNode. Sinon, erreur !
            Element fenced = (mathElement.childNodeSize()==1&&mathElement.children().size()==1) ? mathElement.child(0) : JsoupTools.parse("<mrow></mrow>").select("mrow").first().html(mathElement.html());
            JMathDisplayer innerPane = new JMathDisplayer(fenced, parent);
            setComponent(innerPane, "main");
        }
    }
    /** Handle a hat: <munderover><mrow>x</mrow><mo>^</mo><mo>^</mo></munderover> **/
    public static abstract class AbstractModuleUnderOver extends Module {
        public AbstractModuleUnderOver(Element mathElement, JComponent parent) {
            super(mathElement, parent, new MathLayout.UnderOverLayout());
            Element inner = mathElement.child(0);
            JMathDisplayer innerPane = new JMathDisplayer(inner, parent);
            setComponent(innerPane, "main");
        }
        protected void createChild(Element mathml, JComponent owner, String name) {
            if(!isDrawable(mathml.text().trim())) {
                JMathDisplayer pane = new JMathDisplayer(mathml, owner);
                setComponent(pane, name);
            } else {
                ((MathLayout.UnderOverLayout)getLayout()).setShape(mathml.text().trim().charAt(0),name);
            }
        }
    }
    /** Handle a hat: <munderover><mrow>x</mrow><mo>^</mo><mo>^</mo></munderover> **/
    public static class ModuleUnderOver extends AbstractModuleUnderOver {
        public ModuleUnderOver(Element mathElement, JComponent parent) throws MathMLParsingException {
            super(mathElement, parent);
            if(mathElement.children().size()<3) {throw new MathMLParsingException("not enough children in <munderover> node. Requiered: 3, found: "+mathElement.children().size(),mathElement);}
            createChild(mathElement.child(1), parent, "under");
            createChild(mathElement.child(2), parent, "over");
        }
    }
    /** Handle a hat: <munder><mrow>x</mrow><mo>^</mo></munder> **/
    public static class ModuleUnder extends AbstractModuleUnderOver {
        
        public ModuleUnder(Element mathElement, JComponent parent) throws MathMLParsingException {
            super(mathElement, parent);
            if(mathElement.children().size()<2) {throw new MathMLParsingException("not enough children in <munder> node. Requiered: 2, found: "+mathElement.children().size(),mathElement);}
            createChild(mathElement.child(1), parent, "under");
        }
    }
    /** Handle a hat: <mover><mrow>x</mrow><mo>^</mo></mover> **/
    public static class ModuleOver extends AbstractModuleUnderOver {
        
        public ModuleOver(Element mathElement, JComponent parent) throws MathMLParsingException {
            super(mathElement, parent);
            if(mathElement.children().size()<2) {throw new MathMLParsingException("not enough children in <mover> node. Requiered: 2, found: "+mathElement.children().size(),mathElement);}
            createChild(mathElement.child(1), parent, "over");
        }
    }
    /** Handle a multi-index: <msubsup><mo>&#x222B;</mo><mi>0</mi><mi>1</mi></msubsup> **/
    public static abstract class AbstractModuleMultiscript extends Module {
        public AbstractModuleMultiscript(Element mathElement, JComponent parent) {
            super(mathElement, parent, new MathLayout.MultiScriptLayout());
            if(mathElement.children().isEmpty()) return;
            JMathDisplayer innerPane = new JMathDisplayer(mathElement.child(0), parent);
            setComponent(innerPane, "main");
        }
        protected final void createIndex(Element iElement, JComponent owner, String name) {
            JMathDisplayer iPane = new JMathDisplayer(iElement, owner);
            iPane.setScaleRatio(0.6f);
            iPane.setFontSize(iPane.getFontSize());
            setComponent(iPane, name);
        }
    }
    /** Handle a multi-index: <mmultiscript><mo>&#x222B;</mo><mi>0</mi><mi>1</mi></mmultiscript> **/
    public static class ModuleMultiscript extends AbstractModuleMultiscript {
        public ModuleMultiscript(Element mathElement, JComponent parent) {
            super(mathElement, parent);
            int n = mathElement.children().size();
            if(n<=1) {return;}
            String[] indexes = {"postSub","postSup","preSub","preSup"};
            int index = 0;
            for(int i=1; i<n; i++) {
                Element child = mathElement.child(i);
                switch(child.tagName()) {
                    case "none": break;
                    case "mprescripts": index = 1; break;//We go directly to prescripts
                    default: createIndex(child, parent, indexes[index]);
                }
                index++;
            }
        }
    }
    /** Handle a double index: <msubsup><mo>&#x222B;</mo><mi>0</mi><mi>1</mi></msubsup> **/
    public static class ModuleSubSup extends AbstractModuleMultiscript {
        public ModuleSubSup(Element mathElement, JComponent parent) throws MathMLParsingException {
            super(mathElement, parent);
            if(mathElement.children().size()<3) {throw new MathMLParsingException("not enough children in <msubsup> node. Requiered: 3, found: "+mathElement.children().size(),mathElement);}
            createIndex(mathElement.child(1), parent, "postSub");
            createIndex(mathElement.child(2), parent, "postSup");
        }
    }
    /** Handle an index: <msub><mi>x</mi><mi>i</mi></msub> **/
    public static class ModuleSub extends AbstractModuleMultiscript {
        public ModuleSub(Element mathElement, JComponent parent) {
            super(mathElement, parent);
            createIndex(mathElement.child(1), parent, "postSub");
        }
    }
    /** Handle a power: <msup><mi>x</mi><mi>i</mi></msup> **/
    public static class ModuleSup extends AbstractModuleMultiscript {
        public ModuleSup(Element mathElement, JComponent parent) {
            super(mathElement, parent);
            createIndex(mathElement.child(1), parent, "postSup");
        }
    }
    /** Handle a fence operator: <mo>{</mo> **/
    public static class ModuleFenceOperator extends Module {
        public ModuleFenceOperator(Element mathElement, JComponent parent) {
            super(mathElement, parent, new MathLayout.FenceOperatorLayout());
            Element fence = mathElement;
            ((MathLayout.FenceOperatorLayout)getLayout()).setBracket(fence.text().trim().charAt(0));
            //HACK : we use the sibling to create a fake JMathDisplayer that will give us the correct height for the fence
            Element object = mathElement.nextElementSibling();
            JMathDisplayer objectPane = new JMathDisplayer(object, parent);
            setComponent(objectPane, "main");
        }
    }
    /** Handle a table: <mtable><mtr><mtd>a</mtd><mtd>b</mtd></mtr><mtr><mtd>c</mtd><mtd>d</mtd></mtr></mtable> **/
    public static class ModuleTable extends Module {
        public ModuleTable(Element mathElement, JComponent parent) {
            super(mathElement, parent, new MathLayout.TableLayout());
            int i=0, j=0;
            for(Element rowElement : mathElement.children()) {
                if(rowElement.tagName().equals("mtr") || rowElement.tagName().equals("mlabeledtr")) {
                    for(Element cellElement : rowElement.children()) {
                        JMathDisplayer cell = new JMathDisplayer(cellElement, parent);
                        setComponent(cell, i+","+j);
                        j++;
                    }
                    j=0;
                    i++;
                }
            }
            MathLayout.TableLayout tableLayout = (MathLayout.TableLayout) getLayout();
            if(mathElement.hasAttr("columnspacing")) tableLayout.setColSpacing(Integer.parseInt(mathElement.attr("columnspacing").trim()));
            if(mathElement.hasAttr("rowspacing")) tableLayout.setRowSpacing(Integer.parseInt(mathElement.attr("rowspacing").trim()));
        }
    }
    private static boolean isDrawable(String op) {
        return DrawShape.get(op.charAt(0), DrawShape.LEFT)!=null;
    }
}
