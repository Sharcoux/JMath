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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;

/**
 *
 * @author François Billioud
 */
public abstract class DrawShape {
    /** Constant for upper brackets **/
    public static final int UP = 0;
    /** Constant for lower brackets **/
    public static final int DOWN = 1;
    /** Constant for left side brackets **/
    public static final int LEFT = 2;
    /** Constant for right side brackets **/
    public static final int RIGHT = 3;
    private final int direction;

    protected DrawShape(int direction) {
        this.direction = direction;
    }
    
    public void paint(Graphics2D g2D, int x, int y, int height) {
        this.paint(g2D, x, y, getWidth(height), height);
    }
    public void paint(int width, Graphics2D g2D, int x, int y) {
        this.paint(g2D, x, y, width, getHeight(width));
    }
    public void paint(Graphics2D g2D, int x, int y, int width, int height) {
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        switch(direction) {
            case LEFT:
                paintShape(g2D, x, y, width, height);
                break;
            case RIGHT: 
                g2D.translate(width, 0);
                g2D.scale(-1, 1);
                paintShape(g2D, -x, y, width, height);
                break;
            case UP:
                g2D.rotate(Math.PI/2);
                g2D.translate(0,-width-x*2);
                paintShape(g2D, y, x, height, width);
                break;
            case DOWN:
                g2D.rotate(-Math.PI/2);
                paintShape(g2D, -y, x, height, width);
                break;
        }
    }
    /** Check if the bracket is rotated and so width becomes height and so on **/
    private boolean isRotated() {return direction==UP || direction==DOWN;}
    /**
     * Get the default width needed to draw the shape with the specified height.
     * @param height height available to draw the shape
     * @return the optimal width needed
     */
    public int getWidth(int height) {return (int)(isRotated()?height*ratio():height/ratio());}
    /**
     * Get the default height needed to draw the shape with the specified width.
     * @param width width available to draw the shape
     * @return the optimal height needed
     */
    public int getHeight(int width) {return (int)(isRotated()?width/ratio():width*ratio());}
    /**
     * The ratio between width and height.
     * @return it returns the height/width of a left bracket
     */
    protected abstract double ratio();
    /**
     * Paint on the {@link java.awt.Graphics2D} the left version of the shape
     * in the rectangle (x,y,width,height).
     * The left version of a bracket is, for instance, '(' for parenthesis.
     * @param g2D the graphics to draw on.
     * @param x the x coordinate where to start drawing
     * @param y the y coordinate where to start drawing
     * @param width the width of the drawing in its left version
     * @param height the height of the drawing in its left version
     */
    protected abstract void paintShape(Graphics2D g2D, int x, int y, int width, int height);
    
    /**
     * Create a shape representing a brace {.
     * Draw braces {, }, \u23de and \u23df
     */
    public static class Brace extends DrawShape {
        /** 
         * Create a shape representing a brace { in the specified direction.
         * @param direction the direction in which to represent the brace
         */
        public Brace(int direction) {super(direction);}
        @Override
        public double ratio() {return 100d/15d;}

        @Override
        protected void paintShape(Graphics2D g2D, int x, int y, int width, int height) {
            //top
            CubicCurve2D shape = new CubicCurve2D.Float();
            Point2D.Float p1 = new Point2D.Float(x, y+height*50f/100f);
            Point2D.Float p2 = new Point2D.Float(x+width*20f/15f, y+height*45f/100f);
            Point2D.Float p3 = new Point2D.Float(x, y);
            Point2D.Float p4 = new Point2D.Float(x+width, y+height*2f/100f);
//            shape.setCurve(0F,50F,20F,45F,0F,0F,15F,2F);
            shape.setCurve(p1,p2,p3,p4);
            g2D.draw (shape);
            
            //bottom
            CubicCurve2D shape2 = new CubicCurve2D.Float();
            p2 = new Point2D.Float(p2.x,y+height*55f/100f);
            p3 = new Point2D.Float(x, y+height);
            p4 = new Point2D.Float(p4.x, y+height*98f/100f);
//            shape2.setCurve(0F,50F,20F,55F,0F,100F,15F,98F);
            shape2.setCurve(p1,p2,p3,p4);
            g2D.draw (shape2);
        }
    }
    
    /**
     * Create a shape representing angle brackets ^.
     * Draw angle brackets ^, \u27e8, \u27e9, \ufe3f and \ufe40
     */
    public static class Angle extends DrawShape {
        /**
         * Create a shape representing angle brackets ^ in the specified direction.
         * @param direction the direction in which to represent the angle
         */
        public Angle(int direction) {super(direction);}
        @Override
        public double ratio() {return 4;}

        @Override
        protected void paintShape(Graphics2D g2D, int x, int y, int width, int height) {
            g2D.drawLine(x,y+height/2,x+width,y);
            g2D.drawLine(x,y+height/2,x+width,y+height);
        }
    }
    
    /**
     * Create a shape representing square brackets [.
     * Draw square brackets [, ], \u23b4 and \u23b5
     */
    public static class Square extends DrawShape {
        /**
         * Create a shape representing square brackets ^ in the specified direction.
         * @param direction the direction in which to represent the bracket
         */
        public Square(int direction) {super(direction);}
        @Override
        public double ratio() {return 6;}

        @Override
        protected void paintShape(Graphics2D g2D, int x, int y, int width, int height) {
            g2D.drawLine(x, y, x+width, y);
            g2D.drawLine(x, y+height, x+width, y+height);
            g2D.drawLine(x, y, x, y+height);
        }
    }
    
    /**
     * Create a shape representing ceiling markers.
     * Draw ceiling markers \u2308 and \u2309
     */
    public static class Ceiling extends DrawShape {
        /**
         * Create a shape representing ceiling markers in the specified direction.
         * @param direction the direction in which to represent the bracket
         */
        public Ceiling(int direction) {super(direction);}
        @Override
        public double ratio() {return 6;}

        @Override
        protected void paintShape(Graphics2D g2D, int x, int y, int width, int height) {
            g2D.drawLine(x, y, x+width, y);
            g2D.drawLine(x, y+height, x+width, y+height);
        }
    }
    /**
     * Create a shape representing floor markers.
     * Draw floor markers \u230a and \u230b
     */
    public static class Floor extends DrawShape {
        /**
         * Create a shape representing floor markers in the specified direction.
         * @param direction the direction in which to represent the bracket
         */
        public Floor(int direction) {super(direction);}
        @Override
        public double ratio() {return 6;}

        @Override
        protected void paintShape(Graphics2D g2D, int x, int y, int width, int height) {
            g2D.drawLine(x, y+height, x+width, y+height);
            g2D.drawLine(x, y, x, y+height);
        }
    }
    
    /**
     * Create a shape representing parenthesis.
     * Draw parenthesis (, ), \u23dc and \u23dd
     */
    public static class Parenthese extends DrawShape {
        /**
         * Create a shape representing parenthesis in the specified direction.
         * @param direction the direction in which to represent the bracket
         */
        public Parenthese(int direction) {super(direction);}
        @Override
        public double ratio() {return 4;}

        @Override
        protected void paintShape(Graphics2D g2D, int x, int y, int width, int height) {
            g2D.drawArc(x, y, width, height, 100, 160);
        }
    }
    
    /**
     * Return a DrawShape that draws the specified operator.
     * @param operator the bracket character to represent
     * @return the DrawShape corresponding to the operator.
     */
    public static DrawShape get(char operator) {
        return get(operator,getDirection(operator));
    }
    
    /**
     * Return the shape corresponding to the operator, turned over the specified direction.
     * Note that '(' is left and ')' is right.
     * For instance, '[' - RIGHT would be ']'.
     * @param operator the bracket character to represent
     * @param direction orientation of the shape. Can be {@link #LEFT}, {@link #RIGHT}, {@link #UP} or {@link #DOWN}.
     * @return the DrawShape corresponding to the parameters.
     */
    public static DrawShape get(char operator, int direction) {
        DrawShape shape;
        switch(operator) {
            case '(':
            case ')':
            case '\u23dc':
            case '\u23dd': shape = new DrawShape.Parenthese(direction); break;

            case '[':
            case ']':
            case '\u23b4':
            case '\u23b5': shape = new DrawShape.Square(direction); break;

            case '^':
            case '\u27e8':
            case '\u27e9':
            case '\ufe3f':
            case '\ufe40': shape = new DrawShape.Angle(direction); break;

            case '{':
            case '}':
            case '\u23de' :
            case '\u23df' : shape = new DrawShape.Brace(direction); break;

            case '\u230a' :
            case '\u230b' : shape = new DrawShape.Floor(direction); break;

            case '\u2308' :
            case '\u2309' : shape = new DrawShape.Ceiling(direction); break;

            default: shape = null;
        }
        return shape;
    }
    /**
     * Return the direction corresponding to the operator.
     * For instance, '(' is left and ')' is right.
     * @param operator the bracket character to detect
     * @return the integer identifier for the direction of the current bracket.
     */
    public static int getDirection(char operator) {
        int direction;
        switch(operator) {
            case '(':
            case '[':
            case '{':
            case '\u27e8':
            case '\u230a' :
            case '\u2308' : direction = DrawShape.LEFT; break;
                
            case ')':
            case ']':
            case '}':
            case '\u27e9':
            case '\u230b':
            case '\u2309': direction = DrawShape.RIGHT; break;
                
            case '\u23dc':
            case '\u23b4':
            case '^':
            case '\ufe3f':
            case '\u23de' : direction = DrawShape.UP; break;

            case '\u23dd':
            case '\u23b5':
            case '\ufe40':
            case '\u23df': direction = DrawShape.DOWN; break;
                
            default: direction = 0;
        }
        return direction;
    }
}
