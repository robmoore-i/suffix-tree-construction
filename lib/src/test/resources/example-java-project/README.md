# L Systems

## Overview of L Systems

An L System is a curve that can be drawn by a turtle, by following some
instructions that are generated iteratively as a string. Below is an example to
describe an L System in more detail, for the purposes of this exercise.

#### Example: Dragon Curve

For example, let's say we start with the string "FX". We call this string the
_instruction string_ and because it is the simplest instance of the Dragon Curve
instruction string, we also call it the _axiom_ of the L System. We then take _
iterations_ of this axiom, by applying these replacement rules:

```
X  ==>  X+YF+
Y  ==>  -FX-Y
```

Below is a list showing the first few instruction string iterations for the
Dragon Curve.

```
iteration 0: FX
iteration 1: FX+YF+
iteration 2: FX+YF++-FX-YF+
iteration 3: FX+YF++-FX-YF++-FX+YF+--FX-YF+
```

The strings generated in this way can be interpreted by a _turtle_ program, to
draw pretty shapes! Turtles interpret the commands for different L systems
differently. In this example, the Dragon Curve, we interpret the commands like
this:

```
X  ==>  Do nothing
Y  ==>  Do nothing
F  ==>  Go forward with the pen down
+  ==>  Turn left 90 degrees
-  ==>  Turn right 90 degrees
```

For the Dragon Curve, iteration 10 looks like this, when drawn out by a turtle
using the code in this repository:

```
                ←↑  ←↑          ←↑  ←↑          
               ←↓→ ←↓→         ←↓→ ←↓→          
               ↓→↑←↑→↑←↑       ↓→↑←↑→↑←↑        
               ←↑→↓←↓←↓→       ←↑→↓←↓←↓→        
            ←↑ ↓←↑→↓→↓←↑    ←↑ ↓←↑→↓→↓←↑        
           ←↓→ ←↓←↓→ ←↑→   ←↓→ ←↓←↓→ ←↑→        
           ↓→↑←↑→↓→↑ ↓→    ↓→↑←↑→↓→↑ ↓→         
           ←↑→↑→↑→↓→       ←↑→↑→↑→↓→            
           ↓→↓←↓←↑→↑←↑  ←↑ ↓←↑←↓←↑→↑←↑  ←↑  ←↑  
             ←↑←↓←↓→↓→ ←↓→ ←↓→↑←↓←↓→↓→ ←↓→ ←↓→  
             ↓→↓→↓→↑→↑←↑→↑←↑→↑←↑→↓→↑→↑←↑→↑←↑→↑←↑
               ←↑→↑→↑→↓→↑→↓←↓←↓→↑→↓←↓←↓→↑→↓←↓←↓→
            ←↑ ↓←↑←↓←↑→↑←↑→   ←↑←↑→   ←↑←↑→↓→↓←↑
           ←↓→ ←↑→↑←↓←↓→↓←↓ ↑→↑→↓←↓ ↑→↑→↓←↓→ ←↑→
           ↓→↑←↑←↓→↓→↓→↑→   ←↓→↑→   ←↓→↑→↓→↑ ↓→ 
           ←↑→↑→↑←↓←↑→↑→↑→   ←↓←↓    ←↓←↓←↓→    
           ↓→↓←↓←↑←↓←↑←↓←↑→               ←↑←↑  
             ←↑←↓→↑←↓→↑←↓←↓             ↑→↑→↓→  
             ↓→↓→↑←↑→↑←↑→               ←↓→↑→↑←↑
  ↑→↑→         ←↑→↓←↓←↓→↑→               ←↓←↓←↓→
  ←↓→↓→     ←↑ ↓←↑→   ←↑←↑→         ←↑    ←↓→↓←↑
↑→↑→ ←↓    ←↓→ ←↓←↓ ↑→↑→↓←↓        ←↓   ↑→↑→ ←↑→
←↓→↑       ↓→↑←↑→   ←↓→↑→          ↓→   ←↓→↑ ↓→ 
 ←↓→       ←↑→↑→↑→   ←↓←↓          ←↑→↑→↑←↓→    
  ←↑←↑  ←↑ ↓←↑←↓←↑→                ↓→↓←↓→↓←↑    
↑→↑→↓→ ←↓→ ←↓→↑←↓←↓                  ←↑→ ←↑→    
←↓→↑→↑←↑→↑←↑→↑←↑→                    ↓→  ↓→     
 ←↓←↓←↓→↑→↓←↓←↓→↑→                              
      ←↑←↑→   ←↑←↑→                             
    ↑→↑→↓←↓ ↑→↑→↓←↓                             
    ←↓→↑→   ←↓→↑→                               
     ←↓←↓    ←↓←↓                               
```

## Coding Exercise

Turtle Inc. wants a program for printing L Systems to impress their clients.
Rob (from now on referred to as "The Business") will, at the start of the
exercise, tell you what The Business wants by the end of the first 15 minutes.

At the end of the 15 minutes, The Business will check everyone's progress in
terms of working software.

The Business will then request features for the next 15 minutes and again, look
at working software to check progress. This will continue until The Business is
happy.

## Descriptions of L Systems

### Dragon Curve

#### Axiom

```
FX
```

#### Iteration Rules

```
X  ==>  X+YF+
Y  ==>  -FX-Y
```

#### Turtle Interpretation

```
X  ==>  Do nothing
Y  ==>  Do nothing
F  ==>  Go forward with the pen down
+  ==>  Turn left 90 degrees
-  ==>  Turn right 90 degrees
```

#### Example

```
                ←↑  ←↑          ←↑  ←↑          
               ←↓→ ←↓→         ←↓→ ←↓→          
               ↓→↑←↑→↑←↑       ↓→↑←↑→↑←↑        
               ←↑→↓←↓←↓→       ←↑→↓←↓←↓→        
            ←↑ ↓←↑→↓→↓←↑    ←↑ ↓←↑→↓→↓←↑        
           ←↓→ ←↓←↓→ ←↑→   ←↓→ ←↓←↓→ ←↑→        
           ↓→↑←↑→↓→↑ ↓→    ↓→↑←↑→↓→↑ ↓→         
           ←↑→↑→↑→↓→       ←↑→↑→↑→↓→            
           ↓→↓←↓←↑→↑←↑  ←↑ ↓←↑←↓←↑→↑←↑  ←↑  ←↑  
             ←↑←↓←↓→↓→ ←↓→ ←↓→↑←↓←↓→↓→ ←↓→ ←↓→  
             ↓→↓→↓→↑→↑←↑→↑←↑→↑←↑→↓→↑→↑←↑→↑←↑→↑←↑
               ←↑→↑→↑→↓→↑→↓←↓←↓→↑→↓←↓←↓→↑→↓←↓←↓→
            ←↑ ↓←↑←↓←↑→↑←↑→   ←↑←↑→   ←↑←↑→↓→↓←↑
           ←↓→ ←↑→↑←↓←↓→↓←↓ ↑→↑→↓←↓ ↑→↑→↓←↓→ ←↑→
           ↓→↑←↑←↓→↓→↓→↑→   ←↓→↑→   ←↓→↑→↓→↑ ↓→ 
           ←↑→↑→↑←↓←↑→↑→↑→   ←↓←↓    ←↓←↓←↓→    
           ↓→↓←↓←↑←↓←↑←↓←↑→               ←↑←↑  
             ←↑←↓→↑←↓→↑←↓←↓             ↑→↑→↓→  
             ↓→↓→↑←↑→↑←↑→               ←↓→↑→↑←↑
  ↑→↑→         ←↑→↓←↓←↓→↑→               ←↓←↓←↓→
  ←↓→↓→     ←↑ ↓←↑→   ←↑←↑→         ←↑    ←↓→↓←↑
↑→↑→ ←↓    ←↓→ ←↓←↓ ↑→↑→↓←↓        ←↓   ↑→↑→ ←↑→
←↓→↑       ↓→↑←↑→   ←↓→↑→          ↓→   ←↓→↑ ↓→ 
 ←↓→       ←↑→↑→↑→   ←↓←↓          ←↑→↑→↑←↓→    
  ←↑←↑  ←↑ ↓←↑←↓←↑→                ↓→↓←↓→↓←↑    
↑→↑→↓→ ←↓→ ←↓→↑←↓←↓                  ←↑→ ←↑→    
←↓→↑→↑←↑→↑←↑→↑←↑→                    ↓→  ↓→     
 ←↓←↓←↓→↑→↓←↓←↓→↑→                              
      ←↑←↑→   ←↑←↑→                             
    ↑→↑→↓←↓ ↑→↑→↓←↓                             
    ←↓→↑→   ←↓→↑→                               
     ←↓←↓    ←↓←↓                               
```

### Fractal Binary Tree

#### Axiom

```
0
```

#### Iteration Rules

```
0  ==>  1[0]0
1  ==>  11
```

#### Turtle Interpretation

```
0  ==>  Go forward with the pen down
1  ==>  Go forward with the pen down
[  ==>  Push current position and angle onto the stack & turn left 45 degrees
]  ==>  Pop position and angle from the stack & turn right 45 degrees
```

#### Example

```
     ↖ ↗ ↖ ↗         ↖ ↗ ↖ ↗     
    ↖ ↑   ↑ ↗       ↖ ↑   ↑ ↗    
     ←↖   ↗→         ←↖   ↗→     
    ↙  ↖ ↗  ↘       ↙  ↖ ↗  ↘    
 ↖ ↗    ↑               ↑    ↖ ↗ 
↖ ↑     ↑               ↑     ↑ ↗
 ←↖     ↑               ↑     ↗→ 
↙  ↖    ↑               ↑    ↗  ↘
    ←←←←↖               ↗→→→→    
↖  ↙     ↖             ↗     ↘  ↗
 ←↙       ↖           ↗       ↘→ 
↙ ↓        ↖         ↗        ↓ ↘
 ↙ ↘        ↖       ↗        ↙ ↘ 
             ↖     ↗             
              ↖   ↗              
               ↖ ↗               
                ↑                
                ↑                
                ↑                
                ↑                
                ↑                
                ↑                
                ↑                
                ↑                
                ↑                
                ↑                
                ↑                
                ↑                
                ↑                
                ↑                
                ↑                
                ↑                
```

### Koch Curve

#### Axiom

```
F
```

#### Iteration Rules

```
F  ==>  F+F-F-F+F
```

#### Turtle Interpretation

```
F  ==>  Go forward with the pen down
+  ==>  Turn left by 90 degrees
-  ==>  Turn right by 90 degrees
```

#### Example

```
             ↑
            ↑→
           ↑←↑
          ↑→↓→
         ↑→   
        ↑←↑   
       ↑→↓←↑←↑
      ↑→  ↓←↓→
     ↑←↑  ←↑←↑
    ↑→↓→  ↓→↓→
   ↑→         
  ↑←↑         
 ↑→↓→         
↑→            
←↑            
 ←↑←↑         
  ←↓→         
   ←↑         
    ←↑←↑  ←↑←↑
     ←↓→  ↓→↓→
      ←↑  ←↓←↑
       ←↑←↓→↓→
        ←↓→   
         ←↑   
          ←↑←↑
           ←↓→
            ←↑
```
