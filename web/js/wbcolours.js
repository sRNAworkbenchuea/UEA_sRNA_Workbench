/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


var wbcolors = [];

var WORKBENCH_BLUE = "#336ca6";
var WORKBENCH_HSL = chroma.hex(WORKBENCH_BLUE).hsl();

function setupColours () {

    treeJSON = d3.json("../json/SCD.json", function (error, treeData) {
        for (var i = 0; i < treeData.Filenames.length; i++)
        {
            wbcolors.push(getRandomColor());
        }
        
    });


};

function getWbColourScale(N, startHSL){
    
    return d3.scale.ordinal().range(getWbColourRange(N, startHSL))
}

function getWbColourRange(N, startHSL){
        var H=15, S=WORKBENCH_HSL[1], L=WORKBENCH_HSL[2]
    if(startHSL !== undefined)
    {
       H=startHSL[0], S=startHSL[1], L=startHSL[2];
    }
    var colourWheelSpacing = 360/(N+1);
    var colours = [];
    for(var i=0; i<N; i++)
    {
        var hue = i*colourWheelSpacing + H;
        var hex = chroma.hsl(hue, S, L).hex();
        //console.log([hue, hex])
        colours.push(hex);
    }
    return colours;
}

function getThemeColour()
{
    return WORKBENCH_BLUE;
}

function getColours (filearray) {
    var hash = {};
    filearray.forEach(function(f){
            hash[f] = getRandomColor();
    }
    );
    return hash;
};

function getRandomColor() {
    var letters = '0123456789ABCDEF'.split('');
    var color = '#';
    for (var i = 0; i < 6; i++ ) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}

// Found at http://www.sitepoint.com/javascript-generate-lighter-darker-color/
// hex — a hex color value such as “#abc” or “#123456″ (the hash is optional)
// lum — the luminosity factor, i.e. -0.1 is 10% darker, 0.2 is 20% lighter, etc.
function colorLuminance(hex, lum) {

	// validate hex string
	hex = String(hex).replace(/[^0-9a-f]/gi, '');
	if (hex.length < 6) {
		hex = hex[0]+hex[0]+hex[1]+hex[1]+hex[2]+hex[2];
	}
	lum = lum || 0;

	// convert to decimal and change luminosity
	var rgb = "#", c, i;
	for (i = 0; i < 3; i++) {
		c = parseInt(hex.substr(i*2,2), 16);
		c = Math.round(Math.min(Math.max(0, c + (c * lum)), 255)).toString(16);
		rgb += ("00"+c).substr(c.length);
	}

	return rgb;
}