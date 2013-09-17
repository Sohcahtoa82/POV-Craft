/************************************************
Water based on water designed by Christoph Hormann
http://www.imagico.de/pov/water/index.php
************************************************/

#version 3.7;
#include "functions.inc"

//#declare loc = <130, 77, 145>;
//#declare lookat = <117, 68, 189>;

//#declare loc = <389, 81, 349>;
//#declare lookat = <382, 75, 354>;

//#declare loc = <61,98,13>;
//#declare lookat = <51, 94, 23>;

//#declare loc = <-200,300,-200>;
//#declare lookat = <50,0,150>;

//#declare loc = <-47, 69, -114>;
//#declare lookat = <-38, 69, -114>;

//#declare loc = <195, 72, 568>;
//#declare lookat = <215, 64, 529>;

//#declare loc = <-72, 76, 448>;
//#declare lookat = <-98, 76, 467>;

//#declare loc = <-230, 85, 438>;
//#declare lookat = <-276, 73, 468>;

#declare loc = <-351, 77, 205>;
#declare lookat = <-398, 67, 252>;


#declare blocks = array[158];

#declare i = 0;
#while (i < 158)
        #declare blocks[i] =  box {0, 1 pigment {color rgb 1}  }
        #declare i = i + 1;
#end

default {
        finish { diffuse 0.8 }
}

#declare blocks[1] = box { 0, 1 pigment {color rgb <0.5, 0.5, 0.5> } }  // stone
#declare blocks[2] = box { 0, 1 pigment {color rgb <0, 1, 0> } }  // grass
#declare blocks[3] = box { 0, 1 pigment {color rgb <0.5, 0.4, 0.25> } }  // dirt
#declare blocks[4] = box { 0, 1 pigment {color rgb <0.25,0.25, 0.25> } }  // Cobblestone
#declare blocks[8] = box { 0, 1 pigment {color rgb <0, 0, 1> } }  // water
#declare blocks[9] = box { 0, 1 pigment {color rgb <0, 0, 1> } }  // water
#declare blocks[12] = box { 0, 1 } //pigment {color rgb <.8, .8, .4> } }  // sand

#declare Face = polygon { 5, <0, 0>, <0, 1>, <1, 1>, <1, 0>, <0,0> }

#macro TopFace(image, run, tx)
        object{ Face scale <run,1,1> pigment { image_map {png image}} rotate 90*x translate tx+y }  
#end

#macro BottomFace(image, run, tx)
        object{ Face scale <run,1,1> pigment { image_map {png image}} rotate 90*x translate tx}  
#end

#macro NorthFace(image, run, tx)
        object{ Face scale <run,1,1> pigment { image_map {png image}} translate tx }
#end

#macro SouthFace(image, run, tx)
        object{ Face scale <run,1,1> pigment { image_map {png image}} translate tx + z}
#end

#macro EastFace(image, run, tx)
        object{ Face scale <run,1,1> pigment { image_map {png image}} rotate -90*y translate tx+x }
#end

#macro WestFace(image, run, tx)
        object{ Face scale <run,1,1> pigment { image_map {png image}} rotate -90*y translate tx }
#end

// N S Bot Top W E
#macro MyBoxSimple(image, run)
        union {
                object{ Face scale <run,1,1> pigment { image_map {png image}} }
                object{ Face scale <run,1,1> pigment { image_map {png image}} translate 1*z }
                object{ Face scale <run,1,1> pigment { image_map {png image}} rotate 90*x }  
                object{ Face scale <run,1,1> pigment { image_map {png image}} rotate 90*x translate 1*y }
                object{ Face pigment { image_map {png image}} rotate -90*y }
                object{ Face pigment { image_map {png image}} rotate -90*y translate run*x }
        }
#end

#macro MyBoxComplex(top_image, side_image, bottom_image, run)
        
        union {
                object{ Face scale <run,1,1> pigment { image_map {png side_image}} }
                object{ Face scale <run,1,1> pigment { image_map {png side_image}} translate 1*z }
                object{ Face scale <run,1,1> pigment { image_map {png bottom_image}} rotate 90*x }  
                object{ Face scale <run,1,1> pigment { image_map {png top_image}} rotate 90*x translate 1*y }
                object{ Face pigment { image_map {png side_image}} rotate -90*y }
                object{ Face pigment { image_map {png side_image}} rotate -90*y translate run*x }
        }
#end

// perspective (default) camera
camera {
  location  loc
  look_at   lookat
  right     -x*image_width/image_height
  angle 90
}


 light_source { 
  // put this inside a light_source to make it parallel
  <0,1000,0>
    color rgb <1,1,1>
    area_light
    <50,0,0>,<0,0,50>,5,5
    adaptive 1
    circular
    orient
  parallel
  point_at <-600, 0, -200>

  }



global_settings {

  ambient_light 0
  assumed_gamma 1.0
  //max_trace_level 256
        
  radiosity {
    pretrace_start 64/image_width           // start pretrace at this size
    pretrace_end   2/image_width           // end pretrace at this size
    count 300                      // higher -> higher quality (1..1600) [35]
    nearest_count 5               // higher -> higher quality (1..10) [5]
    error_bound 0.3               // higher -> smoother, less accurate [1.8]
    recursion_limit 1             // how much interreflections are calculated (1..5+) [3]
    low_error_factor 0.5           // reduce error_bound during last pretrace step
    gray_threshold 0.2            // increase for weakening colors (0..1) [0]
    minimum_reuse 2/image_width           // reuse of old radiosity samples [0.015]
    brightness 1                  // brightness of radiosity effects (0..1) [1]

    adc_bailout 3/256
    normal off                   // take surface normals into account [off]
    media off                    // take media into account [off]
    //save_file "file_name"       // save radiosity data
    //load_file "file_name"       // load saved radiosity data
    always_sample off           // turn sampling in final trace off [on]
    //max_sample 1.0              // maximum brightness of samples

  }
}

  sky_sphere {
    pigment {
      bozo
      turbulence 0.65
      octaves 6
      omega 0.7
      lambda 2
      color_map {
          [0.0 0.2 color rgb 1
                   color rgb 1]
          [0.2 0.5 color rgb 1
                   color rgb <.1, .1, 1>]
          [0.5 1.0 color rgb <.1, .1, 1>
                   color rgb <.1, .1, 1>]
      }
      scale <0.2, 0.5, 0.2>
    }
    rotate -135*x
  }
  
#debug "Water...\n"

#declare func1 = function { pattern { bozo turbulence 0.1 } };
#declare func2 = function { pattern { bozo turbulence 0.1 } };
#declare func3 = function { func1(x, y, z)* (cos(clock * 2 * pi) *0.5+ 0.5)};
#declare func4 = function { func2(x, y+10, z)* (-cos(clock * 2 * pi) *0.5+ 0.5)};
#if (clock < 0.5)
        #declare func5 = function { func3(x , y+ (clock + 0.5), z)  };
#else
        #declare func5 = function { func3(x , y+ (clock - 0.5), z)};
#end
//#if (clock < 0.75)
        #declare func6 = function { func4(x, y + (1 - clock), z) };
//#else
        //#declare func6 = function { func4(x , y+ (clock - 0.75), z) };
//#end
#declare funcfinal = function { (func5(x, y, z) + func6(x, y, z)) };// / (cos(clock * 2 * pi) * 0.5 + sin(clock * 2 * pi) * 0.5 + 1) };

#declare waterMaterial = material {
          texture {
            pigment {
              //color rgbf <0.8, 0.8, 0.9, 0.7>
              color rgbf <1,1,1,0.6>
            }
            finish {
              ambient 0.0
              diffuse 0.0
        
              reflection {
                0.0, 1
                fresnel on
              }
        
              //specular 0.4
              //roughness 0.003
            }
            normal {
                function { funcfinal(x, y, z) }
                bump_size 4
              //bozo 
              //turbulence 0.9 scale 10 frequency 0.4
              //scale <0.2,1,0.06>  rotate<0,10,0>
              
              /*pigment_pattern{
                bozo 
                //colour_map { [0, rgb sin(clock*pi*2)]// [0.5, rgb sin(clock*pi*2 + pi/2)]
                 //[1, rgb sin(clock*pi*2 + pi/2)]}
                //color_map { [0, rgbt <0,0,0,abs(0.5-clock)*2+1>] [1, rgbt <1,1,1,abs(0.5-clock)*2+1>] }
                color_map {
                #for (xx, 0, 255/256, 1/255)
                        [xx, rgb sin((clock*pi*2 + xx*pi*2) * (256/256))]
                #end
                }
                //frequency 1.5 sine_wave phase clock turbulence 0.4 
                //scale <2,1,0.4>  rotate<0,20,0>
                //translate 100*x //translate (clock*2 - floor(clock * 2))*y 
              }*/
              
            }
          }
          
          
          
          
          interior {
            ior 1.33
            //fade_distance 1
            //fade_power 20
            media { absorption < 0.18, 0.18, 0.2> }
             
          }
       translate -0.2*y
}


#include "minecraft_water.inc"


#debug "...done\n"
#include "minecraft_glass.inc"

#include "minecraft.inc"
#debug "\nDone parsing.  Pissing myself over the sheer number of objects...\n"
