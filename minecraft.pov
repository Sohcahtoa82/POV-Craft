/************************************************
Credit for water effect goes to Christoph Hormann
http://www.imagico.de/pov/water/index.php
************************************************/

#version 3.7;
#include "functions.inc"

#declare blocks = array[158];

#declare i = 0;
#while (i < 158)
        #declare blocks[i] =  box {0, 1 pigment {color rgb 1}  }
        #declare i = i + 1;
#end


#declare blocks[1] = box { 0, 1 pigment {color rgb <0.5, 0.5, 0.5> } }  // stone
#declare blocks[2] = box { 0, 1 pigment {color rgb <0, 1, 0> } }  // grass
#declare blocks[3] = box { 0, 1 pigment {color rgb <0.5, 0.4, 0.25> } }  // dirt
#declare blocks[4] = box { 0, 1 pigment {color rgb <0.25,0.25, 0.25> } }  // Cobblestone
#declare blocks[8] = box { 0, 1 pigment {color rgb <0, 0, 1> } }  // water
#declare blocks[9] = box { 0, 1 pigment {color rgb <0, 0, 1> } }  // water
#declare blocks[12] = box { 0, 1 } //pigment {color rgb <.8, .8, .4> } }  // sand

//#declare Face = plane { -z, 0.5 }
#declare Face = polygon { 5, <0, 0>, <0, 1>, <1, 1>, <1, 0>, <0,0> }

        /*intersection {
                object {Face pigment { image_map {png image}}}
                object {Face pigment { image_map {png image}} rotate y * 90 }
                object {Face pigment { image_map {png image}} rotate y * 180}
                object {Face pigment { image_map {png image}} rotate y * 270}
                object {Face pigment { image_map {png image}} rotate x * 90 translate <0,run,0>}
                object {Face pigment { image_map {png image}} rotate x * -90}
                //bounded_by { box { -0.6, <0.6,run + 1.6,1.6> } }
        }
        //box { -.5, <.5, .5 + run, .5> pigment { color rgb 1} }*/


// S N Bot Top W E
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

         /*intersection {
                object {Face pigment { image_map {png side_image} translate 0.5*y }}
                object {Face pigment { image_map {png side_image} translate 0.5*y} rotate y * 90 }
                object {Face pigment { image_map {png side_image} translate 0.5*y} rotate y * 180}
                object {Face pigment { image_map {png side_image} translate 0.5*y} rotate y * 270}
                object {Face pigment { image_map {png top_image}} rotate x * 90 translate <0,run,0>}
                object {Face pigment { image_map {png bottom_image}} rotate x * -90}
                //bounded_by { box { -0.6, <0.6,run + 1.6,0.6> } }
        }
        //box { -.5, <.5, .5 + run, .5> pigment { color rgb 1} }*/

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

#macro Water(run, tx)
        /*polygon {    
                5,
                <0, 0, 0>, <0, 0, 1>, <1, 0, 1>, <1, 0, 0>, <0, 0, 0>*/
        box { -0.01, <1.01,run - 0.2,1.01>
        //box { 0, <1,run - 0.2,1>
        //#if (mod(tx.x, 2) = mod(tx.z, 2))
        //        translate <0,0.00001,0>
        //#end

                /*pigment { 
                        bozo scale 1.0 
                        turbulence 0.1
                        frequency 0.1
                        color_map { [0.0 rgbf <0.0, 0.0, 0.5, 0.999>]
                                    //[0.6 rgbf <1,1,1,0.8>]
                                    [1.0 rgbf <0.5,0.5,0.9, 0.999>]}
                        translate -tx
                }*/
                /*
                texture{pigment { rgbf <0,0,0,.9999> } 
              normal { bozo 1 
                       turbulence 0.9 scale 10 frequency 0.4}
              finish { ambient 0.0 diffuse 0.8 
                       brilliance 6.0 phong 0.8 phong_size 120
                       reflection 0.6}
              scale <1.0,1,0.3>*0.20  rotate<0,10,0>  translate -tx*/
              
         }
#end


#declare loc = <130, 77, 145>;
#declare lookat = <117, 68, 189>;

//#declare loc = <61,98,13>;
//#declare lookat = <51, 94, 23>;

//#declare loc = <0,120,-10>;
//#declare lookat = <0,0,0>;


// perspective (default) camera
camera {
  location  loc
  look_at   lookat
  right     -x*image_width/image_height
}

// create a regular point light source
//light_source {
//  loc                  // light's position (translated below)
//  color rgb <1,1,1>    // light's color
  
//}

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
  point_at <-300, 0, -100>

  }



global_settings {

        ambient_light 0
        //max_trace_level 256
        
        radiosity {
    pretrace_start 64/image_width           // start pretrace at this size
    pretrace_end   2/image_width           // end pretrace at this size
    count 3                      // higher -> higher quality (1..1600) [35]
    nearest_count 5               // higher -> higher quality (1..10) [5]
    error_bound 0.3               // higher -> smoother, less accurate [1.8]
    recursion_limit 1             // how much interreflections are calculated (1..5+) [3]
    low_error_factor 1           // reduce error_bound during last pretrace step
    gray_threshold 0.0            // increase for weakening colors (0..1) [0]
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
  
  /*plane{<0,1,0>,1 hollow double_illuminate no_shadow
      
        texture{ pigment {color rgb<0.1,0.35,0.8>*0.8}
                          finish {ambient 1  diffuse 1}
               } // end texture 1

        texture{ pigment{ bozo turbulence 0.75
                          octaves 6  omega 0.7 lambda 2 
                          color_map {
                          [0.0  color rgb <0.95, 0.95, 0.95> ]
                          [0.05  color rgb <1, 1, 1>*1.25 ]
                          [0.15 color rgb <0.85, 0.85, 0.85> ]
                          [0.55 color rgbt <1, 1, 1, 1>*1 ]
                          [1.0 color rgbt <1, 1, 1, 1>*1 ]
                          } // end color_map 
                         translate< 3, 0,-1>
                         scale <0.3, 0.4, 0.2>*3
                        } // end pigment
                 finish {ambient 1 diffuse 1}
               } // end texture 2

      scale 10000} */ 

#debug "Water...\n"

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
                0.0, 1.0
                fresnel on
              }
        
              //specular 0.4
              //roughness 0.003
            }
            normal {
              /*bozo 1 
              turbulence 0.9 scale 10 frequency 0.4 phase clock
              scale <1.0,1,0.3>*0.20  rotate<0,10,0>
              translate <0, sin(clock*2*pi), 0, cos(clock*2*pi)>
              translate <0,0,0>*/
              
              pigment_pattern{
                bozo 
                //colour_map { [0, rgb sin(clock*pi*2)]// [0.5, rgb sin(clock*pi*2 + pi/2)]
                 //[1, rgb sin(clock*pi*2 + pi/2)]}
                //color_map { [0, rgbt <0,0,0,abs(0.5-clock)*2+1>] [1, rgbt <1,1,1,abs(0.5-clock)*2+1>] }
                color_map {
                #for (xx, 0, 1, 1/255)
                        [xx, rgb sin((clock*pi*2 + xx*pi*2) * (255/256))]
                #end
                }
                //frequency 1.5 sine_wave phase clock turbulence 0.4 
                scale <2,1,0.4>  rotate<0,20,0>
                translate 100*x //translate (clock*2 - floor(clock * 2))*y 
              }
              
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

//#include "minecraft.inc"
#debug "\nDone parsing.  Pissing myself over the sheer number of objects...\n"
