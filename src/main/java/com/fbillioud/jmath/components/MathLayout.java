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
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.swing.JPanel;

/**
 * The MathLayout is responsible for the size and position of
 * the children and the lines needed to represent a MathComponent.
 * @author François Billioud
 */
public abstract class MathLayout implements LayoutManager2 {

    protected static enum SIZE {MIN, MAX, PREFERRED, CURRENT}
    protected static Dimension getSize(Component c, SIZE size) {
        if(c==null) {return new Dimension();}
        Dimension d;
        switch(size) {
            case CURRENT: d = c.getSize(); break;
            case MIN: d = c.getMinimumSize(); break;
            case MAX: d = c.getMaximumSize(); break;
            case PREFERRED: d = c.getPreferredSize(); break;
            default : d = null;
        }
        return d;
    }
    
    public void paintLines(Graphics2D g, Container target) {
        Dimension d = layoutSize(target, SIZE.CURRENT);
        int x = (target.getWidth()-d.width)/2;
        int y = (target.getHeight()-d.height)/2;
        paintLines(g, target, x, y);
    }
    abstract void paintLines(Graphics2D g, Container target, int x, int y);
    
    protected abstract Dimension layoutSizeNoMargin(Container target, SIZE size);
    protected Dimension layoutSize(Container target, SIZE size) {
        Dimension d = layoutSizeNoMargin(target, size);
        Insets insets = target.getInsets();
        return new Dimension(d.width+insets.left+insets.right, d.height+insets.top+insets.bottom);
    }
    protected abstract void layoutContainer(Container target, int offsetX, int offsetY);
    protected abstract float layoutYAlignment(Container target, float lineHeight, float height);
    @Override
    public void layoutContainer(Container target) {
        for(Component c : target.getComponents()) {
            c.setSize(c.getPreferredSize());
            c.doLayout();
        }
        Dimension d = layoutSize(target, SIZE.PREFERRED);
        int x = (target.getWidth()-d.width)/2+target.getInsets().left;
        int y = (target.getHeight()-d.height)/2+target.getInsets().top;
        layoutContainer(target, x, y);
//        if(target instanceof JComponent) {
//            FontMetrics fm = target.getFontMetrics(target.getFont());
//            float lineHeight = fm.getAscent();
//            float h = d.height;
//            float yAlign = layoutYAlignment(target, lineHeight, h);
////            if(target.getFontMetrics(target.getFont()).getAscent()>target.getHeight()*yAlign) {yAlign = 1-yAlign;}
//            ((JComponent)target).setAlignmentY(yAlign);
//        }
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        addLayoutComponent(constraints+"" , comp);
    }

    @Override
    public float getLayoutAlignmentX(Container target) {return Component.LEFT_ALIGNMENT;}
    @Override
    public float getLayoutAlignmentY(Container target) {
        FontMetrics fm = target.getFontMetrics(target.getFont());
        float lineHeight = fm.getAscent();
        float h = target.getHeight();
        float yAlign = layoutYAlignment(target, lineHeight, h);
        return yAlign;
    }

    @Override
    public void invalidateLayout(Container target) {}
    @Override
    public Dimension maximumLayoutSize(Container target) {return preferredLayoutSize(target);}
    @Override
    public Dimension preferredLayoutSize(Container target) {return layoutSize(target, SIZE.PREFERRED);}
    @Override
    public Dimension minimumLayoutSize(Container target) {return layoutSize(target, SIZE.MIN);}
    
    protected int getLineWidth(Graphics2D g) {
        return (int) ((BasicStroke)g.getStroke()).getLineWidth();
    }
    
    public static class RowLayout extends MathLayout {
        LinkedList<Component> components = new LinkedList<>();
        @Override
        void paintLines(Graphics2D g, Container target, int x, int y) {}
        
        /**
         * Get the bounds of the container if all components where aligned on 0.
         * @param target the container to layout
         * @param size PREFERRED, MIN, MAX or CURRENT
         * @return the bounds of the target if all components where aligned on 0
         */
        private Rectangle relativeRow(Container target, SIZE size) {
            Rectangle r = new Rectangle(-1, -1);
            int x = 0;
            for(Component c : target.getComponents()) {
                Dimension d = getSize(c, size);
                //We align components on 0 to get the final size and baseline.
                r.add(new Rectangle(new Point(x,-(int)(c.getAlignmentY()*c.getHeight())),d));
                x+=d.width;
            }
            return r;
        }
        
        @Override
        protected Dimension layoutSizeNoMargin(Container target, SIZE size) {
            return relativeRow(target, size).getSize();
        }

        @Override
        protected void layoutContainer(Container target, int offsetX, int offsetY) {
            int x=offsetX;
            int baseLine = -relativeRow(target,SIZE.PREFERRED).y;
            for(Component c : target.getComponents()) {
                c.setLocation(x,offsetY+baseLine-(int)(c.getAlignmentY()*c.getHeight()));
                x+=c.getWidth();
            }
        }

        @Override
        protected float layoutYAlignment(Container target, float lineHeight, float height) {
            float max = 0;
            for(Component c : components) {
                float ha = c.getAlignmentY()*c.getHeight();
                if(max<ha) max = ha;
            }
            return max/height;
        }
        
        @Override
        public void addLayoutComponent(String name, Component comp) {components.add(comp);}
        @Override
        public void removeLayoutComponent(Component comp) {components.remove(comp);}
        
    }
    public static class FracLayout extends MathLayout {
        private Component numerator = null, denominator = null;
        
        private int getPadding(Component target) {return target.getFont().getSize()/8;}
        
        @Override
        public void addLayoutComponent(String name, Component comp) {if((numerator==null && !"denominator".equals(name)) || "numerator".equals(name)) numerator = comp; else denominator = comp;}
        @Override
        public void removeLayoutComponent(Component comp) {if(denominator==comp) denominator = null; else if(numerator==comp) numerator = null;}
        @Override
        public void paintLines(Graphics2D g, Container target, int x,  int y) {
            if(numerator==null || denominator==null) return;
            int margin = getLineWidth(g);
            g.drawLine(x+margin, numerator.getY()+numerator.getHeight(), target.getWidth()-margin-x, numerator.getY()+numerator.getHeight());
        }
        
        @Override
        protected Dimension layoutSizeNoMargin(Container target, SIZE size) {
            Dimension num = getSize(numerator, size);
            Dimension den = getSize(denominator, size);
            int width = Math.max(num.width, den.width);
            int height = num.height+den.height;
            return new Dimension(width+2*getPadding(target), height);
        }
        
        @Override
        public void layoutContainer(Container target, int x, int y) {
            if(numerator==null || denominator==null) {return;}
            numerator.setLocation((target.getWidth()-numerator.getWidth())/2, y);
            denominator.setLocation((target.getWidth()-denominator.getWidth())/2, y+numerator.getHeight());
        }
        
        @Override
        protected float layoutYAlignment(Container target, float lineHeight, float height) {
            return numerator.getHeight()/height+lineHeight/(4f*height);
        }
        
    }
    
    public static class RootLayout extends MathLayout {
        
        private Component innerPane;
        private Component root;
        
        @Override
        public void addLayoutComponent(String name, Component comp) {
            if((innerPane==null&&!"root".equals(name))||"main".equals(name)) innerPane=comp; else root = comp;
        }
        @Override
        public void removeLayoutComponent(Component comp) {if(innerPane==comp) innerPane=null; else if(root==comp) root = null;}
        
        /** Reference used to calculate other dimensions. **/
        private float getReferenceWidth(Component target) {return target.getFont().getSize()/2f;}
        /** The width of the V part of the root. **/
        private float getVRootWidth(Component target) {return root==null ? getReferenceWidth(target) : Math.max(getReferenceWidth(target), root.getPreferredSize().width);}//largeur utilisée pour dessiner le V de la racine
        /** The width of the left little arm of the root. **/
        private float getRootArmWidth(Component target) {return getReferenceWidth(target)/5f;}
        /** The padding around the innerPane. **/
        private float getPadding(Component target) {return getReferenceWidth(target)/8f;}
        
        @Override
        public void paintLines(Graphics2D g, Container target, int x, int y) {
            if(innerPane==null) {return;}
            int margin = getLineWidth(g);
            Rectangle bounds = innerPane.getBounds();
            float arm = getRootArmWidth(target);
            float halfRoot = (getVRootWidth(target)-margin)/2;
            int offset = x+margin;
//            int h = (int) (bounds.height/2+arm)+y;
            int lineHeight = target.getFontMetrics(innerPane.getFont()).getAscent();
            int h = (int) (bounds.height*innerPane.getAlignmentY()+getPadding(target)+arm-lineHeight/3f)+y;

            int[] xPoints = {offset, (offset+=arm), offset+=halfRoot, offset+=halfRoot, offset+=(bounds.width-margin)};
            int[] yPoints = {h, h-=arm, target.getHeight()-(int)(getVRootWidth(target)/3)-y, y+margin, y+margin};
            g.drawPolyline(xPoints, yPoints, 5);
        }
        
        @Override
        protected Dimension layoutSizeNoMargin(Container target, SIZE size) {
            if(innerPane==null) {return new Dimension();}
            Dimension inner = getSize(innerPane, size);
            return new Dimension((int)(inner.width+getRootArmWidth(target)+getVRootWidth(target)), (int)(inner.height+getPadding(target)*2));
        }

        @Override
        public void layoutContainer(Container target, int x, int y) {
            if(innerPane==null) {return;}
            innerPane.setLocation((int)(x+getRootArmWidth(target)+getVRootWidth(target)),y+(int)getPadding(target));
            if(root!=null) {
                float lineHeight = target.getFontMetrics(target.getFont()).getAscent();
                root.setLocation(x, (int)(y+innerPane.getHeight()*innerPane.getAlignmentY()-lineHeight/3-root.getHeight()));
            }
        }
        
        @Override
        protected float layoutYAlignment(Container target, float lineHeight, float height) {
            return innerPane == null ? 0f : (innerPane.getAlignmentY()*innerPane.getHeight()+getPadding(target))/height;
        }
        
    }
    public static class SQRTLayout extends RootLayout {
        @Override
        public void addLayoutComponent(String name, Component comp) {super.addLayoutComponent("main", comp);}
    }
    
    public static class UnderOverLayout extends MathLayout {
        private Component innerPane;
        private Component overPane;
        private Component underPane;
        
        private DrawShape overShape;
        private DrawShape underShape;

        private int getOverHeight() {
            if(overPane!=null) return overPane.getPreferredSize().height;
            else if(overShape!=null) return innerPane.getFont().getSize()/4;
            else return 0;
        }
        private int getUnderHeight() {
            if(underPane!=null) return underPane.getPreferredSize().height;
            else if(underShape!=null) return innerPane.getFont().getSize()/4;
            else return 0;
        }
        public void setShape(char operator, String name) {
            if(name.equals("under")) {
                underShape = DrawShape.get(operator, DrawShape.DOWN);
            } else {
                overShape = DrawShape.get(operator, DrawShape.UP);
            }
        }
        
        @Override
        void paintLines(Graphics2D g, Container target, int x, int y) {
            if(innerPane==null) {return;}
            int margin = getLineWidth(g);
            if(underShape!=null) underShape.paint(g, x+margin, y+margin+innerPane.getHeight()+getOverHeight(), innerPane.getWidth()-2*margin, getUnderHeight());
            if(overShape!=null) overShape.paint(g, x+margin, y+margin, innerPane.getWidth()-2*margin, getOverHeight());
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
            if((innerPane==null&&!"over".equals(name)&&!"under".equals(name))||"main".equals(name)) { innerPane=comp; }
            else if((underPane==null&&!"over".equals(name))||"under".equals(name)) { underPane = comp; }
            else { overPane = comp; }
        }
        @Override
        public void removeLayoutComponent(Component comp) {
            if(innerPane==comp) innerPane=null;
            else if(underPane==comp) underPane = null;
            else if(overPane==comp) overPane = null;
        }
        @Override
        protected Dimension layoutSizeNoMargin(Container target, SIZE size) {
            Dimension inner = getSize(innerPane, size);
            return new Dimension(inner.width, inner.height+getOverHeight()+getUnderHeight());
        }
        
        @Override
        public void layoutContainer(Container target, int x, int y) {
            if(innerPane==null) return;
            innerPane.setLocation(x,y+getOverHeight());
            if(overPane!=null) {overPane.setLocation(x, y);}
            if(underPane!=null) {underPane.setLocation(x, y+innerPane.getHeight()+getOverHeight());}
        }
        
        @Override
        protected float layoutYAlignment(Container target, float lineHeight, float height) {
//            return 1-getYAlignment(target)+getOverHeight()/(float)target.getPreferredSize().height;
            return (lineHeight+getOverHeight())/height;
        }
    }
    public static class MultiScriptLayout extends MathLayout {
        private Component corePane;
        private Component postSubPane;
        private Component postSupPane;
        private Component preSubPane;
        private Component preSupPane;
        @Override
        void paintLines(Graphics2D g, Container target, int x, int y) {}

        @Override
        public void addLayoutComponent(String name, Component comp) {
            if((corePane==null&&!"preSub".equals(name)&&!"preSup".equals(name)&&!"postSub".equals(name)&&!"postSup".equals(name))||"main".equals(name)) corePane=comp;
            else if((preSubPane==null&&!"preSup".equals(name)&&!"postSub".equals(name)&&!"postSup".equals(name))||"preSub".equals(name)) preSubPane = comp;
            else if((preSupPane==null&&!"postSub".equals(name)&&!"postSup".equals(name))||"preSup".equals(name)) preSupPane = comp;
            else if((postSubPane==null&&!"postSup".equals(name))||"postSub".equals(name)) postSubPane = comp;
            else postSupPane = comp;
        }
        @Override
        public void removeLayoutComponent(Component comp) {
            if(comp==this.corePane) {this.corePane=null;}
            else if(comp==this.preSubPane) {this.preSubPane=null;}
            else if(comp==this.preSupPane) {this.preSupPane=null;}
            else if(comp==this.postSubPane) {this.postSubPane=null;}
            else if(comp==this.postSupPane) {this.postSupPane=null;}
        }
        @Override
        protected Dimension layoutSizeNoMargin(Container target, SIZE size) {
            Dimension core = getSize(corePane, size);
            Dimension preSub = getSize(preSubPane, size);
            Dimension preSup = getSize(preSupPane, size);
            Dimension postSub = getSize(postSubPane, size);
            Dimension postSup = getSize(postSupPane, size);
            return new Dimension(core.width+Math.max(preSub.width,preSup.width)+Math.max(postSub.width, postSup.width),
                    core.height+Math.max(preSub.height,postSub.height)/4);
        }

        @Override
        public void layoutContainer(Container target, int x, int y) {
            int preWidth = Math.max(getSize(preSubPane, SIZE.CURRENT).width,getSize(preSupPane, SIZE.CURRENT).width);
            if(corePane!=null) corePane.setLocation(x+preWidth,y);
            if(preSubPane!=null) preSubPane.setLocation(x+preWidth-preSubPane.getWidth(),y+corePane.getHeight()-(3*preSubPane.getHeight())/4);
            if(preSupPane!=null) preSupPane.setLocation(x+preWidth-preSupPane.getWidth(),y);
            if(postSubPane!=null) postSubPane.setLocation(x+preWidth+corePane.getWidth(),y+corePane.getHeight()-(3*postSubPane.getHeight())/4);
            if(postSupPane!=null) postSupPane.setLocation(x+preWidth+corePane.getWidth(),y);
        }
        
        @Override
        protected float layoutYAlignment(Container target, float lineHeight, float height) {
            return lineHeight/height;
        }
    }
    
    
    public static class FencedLayout extends MathLayout {
        private Component innerPane;
        
        //TODO : Add separators
        private DrawShape leftBracket = DrawShape.get('(', DrawShape.LEFT);
        private DrawShape rightBracket = DrawShape.get('(', DrawShape.RIGHT);

        public FencedLayout() {}
        public FencedLayout(char open, char close) {
            leftBracket = DrawShape.get(open);
            rightBracket = DrawShape.get(close);
        }

        public void setBracket(char operator, boolean open) {
            if(open) {
                leftBracket = DrawShape.get(operator);
            } else {
                rightBracket = DrawShape.get(operator);
            }
        }
        /** 
         * Espace autour du champ entouré par la targethèse.
         * Càd marge à gauche, à droite, et hauteur supplémentaire de l targethèse en haut et en bas
         **/
        private int getInnerPadding() {return 0;}
//        private int getInnerPadding() {return innerPane==null ? 0 : innerPane.getFont().getSize()/8;}
        
        @Override
        void paintLines(Graphics2D g, Container target, int x, int y) {
            int margin = getLineWidth(g);
            int width = target.getWidth(), height = target.getHeight();
            if(leftBracket!=null) leftBracket.paint(g, x+margin, y+margin, height-2*margin);
            if(rightBracket!=null) rightBracket.paint(g, width-rightBracket.getWidth(height-2*margin)-margin-x, y+margin, height-2*margin);
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {this.innerPane = comp;}
        @Override
        public void removeLayoutComponent(Component comp) {if(comp==this.innerPane) {this.innerPane=null;}}
        @Override
        protected Dimension layoutSizeNoMargin(Container target, SIZE size) {
            int innerPadding = getInnerPadding();
            Dimension inner = getSize(innerPane, size);
            int lWidth = leftBracket==null ? 0 : leftBracket.getWidth(inner.height);
            int rWidth = rightBracket==null ? 0 : rightBracket.getWidth(inner.height);
            return new Dimension(inner.width+lWidth+rWidth, inner.height+2*innerPadding);
        }

        @Override
        public void layoutContainer(Container target, int x, int y) {
            if(innerPane==null) {return;}
            int lWidth = leftBracket==null ? 0 : leftBracket.getWidth(innerPane.getHeight());
            innerPane.setLocation(x+lWidth,y);
        }
        
        @Override
        protected float layoutYAlignment(Container target, float lineHeight, float height) {
            return innerPane==null ? 0 : innerPane.getAlignmentY();
        }
    }
    
    public static class FenceOperatorLayout extends MathLayout {
        /** Sibling used for sizing the fence **/
        private Component siblingPane;
        private DrawShape fenceShape = DrawShape.get('(', DrawShape.LEFT);

        public FenceOperatorLayout() {}
        public FenceOperatorLayout(char fence) {
            fenceShape = DrawShape.get(fence);
        }
        
        public void setBracket(char bracket) {
            this.fenceShape = DrawShape.get(bracket);
        }
        
        public void setSiblingPane(Component sibling) {
            this.siblingPane = sibling;
        }
        
        @Override
        void paintLines(Graphics2D g, Container target, int x, int y) {
            int margin = getLineWidth(g);
            if(fenceShape!=null) fenceShape.paint(g, x+margin, y+margin,target.getWidth()-2*margin, target.getHeight()-2*margin);
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {this.siblingPane = comp;comp.setVisible(false);}
        @Override
        public void removeLayoutComponent(Component comp) {if(comp==this.siblingPane) {this.siblingPane=null;comp.setVisible(true);}}
        @Override
        public void layoutContainer(Container target, int x, int y) {
            if(siblingPane!=null) siblingPane.setSize(siblingPane.getPreferredSize());//Ne sera pas fait par le target car n'appartient pas au target.
        }
        @Override
        protected Dimension layoutSizeNoMargin(Container target, SIZE size) {
            Dimension fake = getSize(siblingPane, size);
            if(fenceShape==null) return new Dimension();
            return new Dimension(fenceShape.getWidth(fake.height), fake.height);
        }
        @Override
        protected float layoutYAlignment(Container target, float lineHeight, float height) {
            if(siblingPane!=null) {siblingPane.doLayout();return siblingPane.getAlignmentY();}
            return lineHeight/height;
        }
    }
    
    public static class TableLayout extends MathLayout {
        private final LinkedList<LinkedList<Component>> table = new LinkedList<>();
        private boolean border = false;
        private int colCellSpace = 4;
        private int rowCellSpace = 0;
        private int colSpace = colCellSpace;
        private int rowSpace = rowCellSpace;
        
        public void setRowSpacing(int spacing) {rowCellSpace = spacing;rowSpace=spacing;}
        public void setColSpacing(int spacing) {colCellSpace = spacing;colSpace=spacing;}
        public void setDrawBorder(boolean draw) {border=draw;}
        
        //TODO : Implement row/column alignment, row/column border, global frame border/alignment
        
        private LinkedList<Integer> getRowHeights(SIZE size) {
            LinkedList<Integer> heights = new LinkedList<>();
            int sum = rowSpace;
            for(LinkedList<Component> row : table) {
                int height = 0;
                for(Component cell : row) {
                    int s = getSize(cell, size).height;
                    if(height<s) {height = s;}
                }
                heights.add(height);
                sum+=height+rowSpace;
            }
            heights.add(sum);//Optimisation : we calculate the sum and add it as last element
            return heights;
        }
        
        private LinkedList<Integer> getColWidths(SIZE size) {
            LinkedList<Integer> widths = new LinkedList<>();
            if(table.isEmpty()) {return widths;}
            LinkedList<Component> row0 = table.get(0);
            for(Component cell : row0) {
                widths.add(getSize(cell, size).width);
            }
            for(LinkedList<Component> row : table) {
                ListIterator<Integer> widthIter = widths.listIterator();
                for(Component cell : row) {
                    int width = widthIter.next();
                    int s = getSize(cell, size).width;
                    if(width<s) {widthIter.set(s);}
                }
            }
            
            //We add the sum as last element
            int sum = colSpace;
            for(int width : widths) {sum+=width+colSpace;}
            widths.add(sum);
            return widths;
        }
        
        @Override
        void paintLines(Graphics2D g, Container target, int offsetX, int offsetY) {
            if(border) {
                int line = getLineWidth(g);
                int col = Math.max(colCellSpace, line);
                if(col!=colSpace) {colSpace=col; target.invalidate();}
                int row = Math.max(rowCellSpace, line);
                if(row!=rowSpace) {rowSpace=row; target.invalidate();}
                
                LinkedList<Integer> widths = getColWidths(SIZE.CURRENT), heights = getRowHeights(SIZE.CURRENT);
                int W = widths.removeLast(), H = heights.removeLast();
                
                //verticals
                int x = colSpace+offsetX;
                g.drawLine(x, offsetY+rowSpace, x, H+offsetY);
                for(Integer width : widths) {
                    x+=width+colSpace;
                    g.drawLine(x, offsetY+rowSpace, x, H+offsetY);
                }
                
                //horizontals
                int y = rowSpace+offsetY;
                g.drawLine(offsetX+colSpace, y, W+offsetX, y);
                for(Integer height : heights) {
                    y+=height+rowSpace;
                    g.drawLine(offsetX+colSpace, y, W+offsetX, y);
                }
            }
        }

        @Override
        protected Dimension layoutSizeNoMargin(Container target, SIZE size) {
            LinkedList<Integer> widths = getColWidths(size), heights = getRowHeights(size);
            int W = widths.removeLast(), H = heights.removeLast();
            return new Dimension(W+(widths.size()+1)*colSpace, H+(heights.size()+1)*rowSpace);
        }

        @Override
        protected void layoutContainer(Container target, int offsetX, int offsetY) {
            LinkedList<Integer> widths = getColWidths(SIZE.CURRENT), heights = getRowHeights(SIZE.CURRENT);
            int y = 0;
            ListIterator<Integer> h = heights.listIterator();
            for(LinkedList<Component> row : table) {
                y+=rowSpace;
                int x = 0;
                ListIterator<Integer> w = widths.listIterator();
                for(Component cell : row) {
                    x+=colSpace;
                    cell.setLocation(offsetX+x, offsetY+y);
                    x+=w.next();
                }
                y+=h.next();
            }
        }

        @Override
        protected float layoutYAlignment(Container target, float lineHeight, float height) {
            return 0.5f+lineHeight/(4f*height);
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
            if(name==null) name = "0,0";
            String[] T = name.split(",");
            int row,col;
            if(T.length<2) {row=0;col=0;}
            else {row = Integer.parseInt(T[0]); col = Integer.parseInt(T[1]);}
            while(table.size()<row+1) {table.add(new LinkedList<Component>());}
            LinkedList<Component> r = table.get(row);
            while(r.size()<col+1) {r.add(new JPanel());}
            r.set(col, comp);
        }

        @Override
        public void removeLayoutComponent(Component comp) {
            for(LinkedList<Component> row : table) {
                row.remove(comp);
            }
        }
        
    }
}
