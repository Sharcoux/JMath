# JMath
Swing Component to display MathML

This library provides a component that can display MathML. It is meant to be better than JEuclid in supporting mover and munder tags, and automatically aligning the component with the text (espacially useful to write fractions).

To create a new component, simply do:

    JMathDisplayer component = new JMathDisplayer(testString);

All tags are currently supported, but very few attributes are supported. Other attributes will be supported if needed. Feel free to ask, or participate.

Attributes currently supported:

- mathcolor
- color
- columnspacing
- rowspacing
