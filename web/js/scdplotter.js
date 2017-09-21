/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var selected = {RAW: true, PT: false, UQ: false, TM: false, Q: false, B: false, DES: false, COMP: false, R: false, NR: true, UM: false, M: false, All: true};

function buildSCD()
{
    updateSelections();
    build_SCD_plot();
}
function updateSelections()
{
    selected.RAW = JSON.parse(localStorage.RAW);
    selected.PT  = JSON.parse(localStorage.PT);
    selected.UQ  = JSON.parse(localStorage.UQ);
    selected.TM  = JSON.parse(localStorage.TM);
    selected.Q   = JSON.parse(localStorage.Q);
    selected.B   = JSON.parse(localStorage.B);
    selected.DES = JSON.parse(localStorage.DES);
    
    selected.COMP = JSON.parse(localStorage.COMP);
    selected.R = JSON.parse(localStorage.R);
    selected.NR = JSON.parse(localStorage.NR);
    
    selected.UM = JSON.parse(localStorage.UM);
    selected.M = JSON.parse(localStorage.M);
    selected.M = JSON.parse(localStorage.ALL);

}



function build_SCD_plot()
{
    treeJSON = d3.json("../json/SCD.json", function (error, treeData) {
// Wrapping in nv.addGraph allows for '0 timeout render', stores rendered charts in nv.graphs, and may do more in the future... it's NOT required
        var chart;

        var YAxis = "";
        if(selected.UM)
        {
            if(selected.COMP)
            {
                YAxis = "Unmapped Complexity";
            }
            else if(selected.R)
            {
                YAxis = "Unmapped Redundant Count";
            }
            else if(selected.NR)
            {
                YAxis = "Unmapped Nonredundant Count";
            }
        }
        else if(selected.M)
        {
            if(selected.COMP)
            {
                YAxis = "Mapped Complexity";
            }
            else if(selected.R)
            {
                YAxis = "Mapped Redundant Count";
            }
            else if(selected.NR)
            {
                YAxis = "Mapped Nonredundant Count";
            }
        }

        nv.addGraph(function () {
            chart = nv.models.lineChart()
                    .options({
                        margin: {left: 100, bottom: 100},
                        x: function (d, i) {
                            return i
                        },
                        showXAxis: true,
                        showYAxis: true,
                        transitionDuration: 850
                    })
                    .useInteractiveGuideline(true)
                    ;

            // chart sub-models (ie. xAxis, yAxis, etc) when accessed directly, return themselves, not the parent chart, so need to chain separately
            chart.xAxis
                    .axisLabel("Count")
                    ;

            chart.yAxis
                    .axisLabel(YAxis)

                    ;

            d3.select('#chart1 svg')
                    .datum(Plot())
                    .call(chart);
            
            

            //TODO: Figure out a good way to do this automatically
            nv.utils.windowResize(chart.update);
            //nv.utils.windowResize(function() { d3.select('#chart1 svg').call(chart) });

            chart.dispatch.on('stateChange', function (e) {
                nv.log('New State:', JSON.stringify(e));
            });

            return chart;
        });
        function find(arr, key, val) { // Find array element which has a key value of val 
            for (var ai, i = arr.length; i--; )
                if ((ai = arr[i]) && ai[key] == val)
                    return ai;
            return null;
        }
        function Plot() {

            var sampleArray = [];

            for (var i = 0; i < treeData.Filenames.length; i++)
            {
                var sample = [];

                for (var sizeClassIndex = 0; sizeClassIndex < 50; sizeClassIndex++)
                {
                    sample.push({x: (sizeClassIndex), y: 0});

                }
                if (selected.RAW)
                {
                    //alert("RAW");
                    var result = find(treeData.Filenames[i].Normalisations, "Normalisation", "NONE");
                    console.log(result);
                    for (var sizeClassIndex = 0; sizeClassIndex < result.SizeClasses.length; sizeClassIndex++)
                    {
                        if (selected.UM)
                        {
                            if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                            {
                                if (selected.COMP)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.Complexity};
                                }
                                else if (selected.NR)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.NonRedundantCount};
                                }
                                else if (selected.R)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.RedundantCount};
                                }
                            }
                        }
                        else if (selected.M)
                        {
                            if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                            {
                                if (selected.COMP)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.Complexity};
                                }
                                else if (selected.NR)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.NonRedundantCount};
                                }
                                else if (selected.R)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.RedundantCount};
                                }
                            }
                        }
                        else if(selected.All)
                        {
                            if (selected.COMP)
                            {
                                var complexity = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    complexity += result.SizeClasses[sizeClassIndex].Mapped.Complexity;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    complexity += result.SizeClasses[sizeClassIndex].Unmapped.Complexity;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: complexity};
                            }
                            else if (selected.NR)
                            {
                                var NRCount = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    NRCount += result.SizeClasses[sizeClassIndex].Mapped.NonRedundantCount;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    NRCount += result.SizeClasses[sizeClassIndex].Unmapped.NonRedundantCount;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: NRCount};
                            }
                            else if (selected.R)
                            {
                                var RCount = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    RCount += result.SizeClasses[sizeClassIndex].Mapped.RedundantCount;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    RCount += result.SizeClasses[sizeClassIndex].Unmapped.RedundantCount;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: RCount};
                            }
                            
                        }
                        //alert(sizeClassIndex);
                        


                    }

                }
                else if (selected.PT)
                {
                    //alert("PT");
                    var result = find(treeData.Filenames[i].Normalisations, "Normalisation", "PER_TOTAL");
                    console.log(result);
                    for (var sizeClassIndex = 0; sizeClassIndex < result.SizeClasses.length; sizeClassIndex++)
                    {
                        if (selected.UM)
                        {
                            if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                            {
                                if (selected.COMP)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.Complexity};
                                }
                                else if (selected.NR)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.NonRedundantCount};
                                }
                                else if (selected.R)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.RedundantCount};
                                }
                            }
                        }
                        else if (selected.M)
                        {
                            if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                            {
                                if (selected.COMP)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.Complexity};
                                }
                                else if (selected.NR)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.NonRedundantCount};
                                }
                                else if (selected.R)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.RedundantCount};
                                }
                            }
                        }
                        else if(selected.All)
                        {
                            if (selected.COMP)
                            {
                                var complexity = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    complexity += result.SizeClasses[sizeClassIndex].Mapped.Complexity;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    complexity += result.SizeClasses[sizeClassIndex].Unmapped.Complexity;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: complexity};
                            }
                            else if (selected.NR)
                            {
                                var NRCount = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    NRCount += result.SizeClasses[sizeClassIndex].Mapped.NonRedundantCount;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    NRCount += result.SizeClasses[sizeClassIndex].Unmapped.NonRedundantCount;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: NRCount};
                            }
                            else if (selected.R)
                            {
                                var RCount = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    RCount += result.SizeClasses[sizeClassIndex].Mapped.RedundantCount;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    RCount += result.SizeClasses[sizeClassIndex].Unmapped.RedundantCount;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: RCount};
                            }
                            
                        }
                        //alert(sizeClassIndex);
                        


                    }

                }
                else if (selected.UQ)
                {
                    var result = find(treeData.Filenames[i].Normalisations, "Normalisation", "UPPER_QUARTILE");
                    for (var sizeClassIndex = 0; sizeClassIndex < result.SizeClasses.length; sizeClassIndex++)
                    {
                        if (selected.UM)
                        {
                            if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                            {
                                if (selected.COMP)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.Complexity};
                                }
                                else if (selected.NR)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.NonRedundantCount};
                                }
                                else if (selected.R)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.RedundantCount};
                                }
                            }
                        }
                        else if (selected.M)
                        {
                            if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                            {
                                if (selected.COMP)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.Complexity};
                                }
                                else if (selected.NR)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.NonRedundantCount};
                                }
                                else if (selected.R)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.RedundantCount};
                                }
                            }
                        }
                        else if(selected.All)
                        {
                            if (selected.COMP)
                            {
                                var complexity = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    complexity += result.SizeClasses[sizeClassIndex].Mapped.Complexity;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    complexity += result.SizeClasses[sizeClassIndex].Unmapped.Complexity;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: complexity};
                            }
                            else if (selected.NR)
                            {
                                var NRCount = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    NRCount += result.SizeClasses[sizeClassIndex].Mapped.NonRedundantCount;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    NRCount += result.SizeClasses[sizeClassIndex].Unmapped.NonRedundantCount;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: NRCount};
                            }
                            else if (selected.R)
                            {
                                var RCount = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    RCount += result.SizeClasses[sizeClassIndex].Mapped.RedundantCount;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    RCount += result.SizeClasses[sizeClassIndex].Unmapped.RedundantCount;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: RCount};
                            }
                            
                        }
                        //alert(sizeClassIndex);
                        


                    }

                }
                else if (selected.TM)
                {
                    var result = find(treeData.Filenames[i].Normalisations, "Normalisation", "TRIMMED_MEAN");
                    for (var sizeClassIndex = 0; sizeClassIndex < result.SizeClasses.length; sizeClassIndex++)
                    {
                        if (selected.UM)
                        {
                            if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                            {
                                if (selected.COMP)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.Complexity};
                                }
                                else if (selected.NR)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.NonRedundantCount};
                                }
                                else if (selected.R)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.RedundantCount};
                                }
                            }
                        }
                        else if (selected.M)
                        {
                            if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                            {
                                if (selected.COMP)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.Complexity};
                                }
                                else if (selected.NR)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.NonRedundantCount};
                                }
                                else if (selected.R)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.RedundantCount};
                                }
                            }
                        }
                        else if(selected.All)
                        {
                            if (selected.COMP)
                            {
                                var complexity = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    complexity += result.SizeClasses[sizeClassIndex].Mapped.Complexity;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    complexity += result.SizeClasses[sizeClassIndex].Unmapped.Complexity;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: complexity};
                            }
                            else if (selected.NR)
                            {
                                var NRCount = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    NRCount += result.SizeClasses[sizeClassIndex].Mapped.NonRedundantCount;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    NRCount += result.SizeClasses[sizeClassIndex].Unmapped.NonRedundantCount;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: NRCount};
                            }
                            else if (selected.R)
                            {
                                var RCount = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    RCount += result.SizeClasses[sizeClassIndex].Mapped.RedundantCount;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    RCount += result.SizeClasses[sizeClassIndex].Unmapped.RedundantCount;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: RCount};
                            }
                            
                        }
                        //alert(sizeClassIndex);
                        


                    }

                }
                else if (selected.Q)
                {
                    var result = find(treeData.Filenames[i].Normalisations, "Normalisation", "QUANTILE");
                    for (var sizeClassIndex = 0; sizeClassIndex < result.SizeClasses.length; sizeClassIndex++)
                    {
                        if (selected.UM)
                        {
                            if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                            {
                                if (selected.COMP)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.Complexity};
                                }
                                else if (selected.NR)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.NonRedundantCount};
                                }
                                else if (selected.R)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.RedundantCount};
                                }
                            }
                        }
                        else if (selected.M)
                        {
                            if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                            {
                                if (selected.COMP)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.Complexity};
                                }
                                else if (selected.NR)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.NonRedundantCount};
                                }
                                else if (selected.R)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.RedundantCount};
                                }
                            }
                        }
                        else if(selected.All)
                        {
                            if (selected.COMP)
                            {
                                var complexity = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    complexity += result.SizeClasses[sizeClassIndex].Mapped.Complexity;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    complexity += result.SizeClasses[sizeClassIndex].Unmapped.Complexity;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: complexity};
                            }
                            else if (selected.NR)
                            {
                                var NRCount = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    NRCount += result.SizeClasses[sizeClassIndex].Mapped.NonRedundantCount;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    NRCount += result.SizeClasses[sizeClassIndex].Unmapped.NonRedundantCount;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: NRCount};
                            }
                            else if (selected.R)
                            {
                                var RCount = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    RCount += result.SizeClasses[sizeClassIndex].Mapped.RedundantCount;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    RCount += result.SizeClasses[sizeClassIndex].Unmapped.RedundantCount;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: RCount};
                            }
                            
                        }
                        //alert(sizeClassIndex);
                        


                    }

                }
                else if (selected.B)
                {
                    var result = find(treeData.Filenames[i].Normalisations, "Normalisation", "BOOTSTRAP");
                    for (var sizeClassIndex = 0; sizeClassIndex < result.SizeClasses.length; sizeClassIndex++)
                    {
                        if (selected.UM)
                        {
                            if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                            {
                                if (selected.COMP)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.Complexity};
                                }
                                else if (selected.NR)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.NonRedundantCount};
                                }
                                else if (selected.R)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.RedundantCount};
                                }
                            }
                        }
                        else if (selected.M)
                        {
                            if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                            {
                                if (selected.COMP)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.Complexity};
                                }
                                else if (selected.NR)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.NonRedundantCount};
                                }
                                else if (selected.R)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.RedundantCount};
                                }
                            }
                        }
                        else if(selected.All)
                        {
                            if (selected.COMP)
                            {
                                var complexity = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    complexity += result.SizeClasses[sizeClassIndex].Mapped.Complexity;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    complexity += result.SizeClasses[sizeClassIndex].Unmapped.Complexity;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: complexity};
                            }
                            else if (selected.NR)
                            {
                                var NRCount = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    NRCount += result.SizeClasses[sizeClassIndex].Mapped.NonRedundantCount;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    NRCount += result.SizeClasses[sizeClassIndex].Unmapped.NonRedundantCount;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: NRCount};
                            }
                            else if (selected.R)
                            {
                                var RCount = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    RCount += result.SizeClasses[sizeClassIndex].Mapped.RedundantCount;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    RCount += result.SizeClasses[sizeClassIndex].Unmapped.RedundantCount;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: RCount};
                            }
                            
                        }
                        //alert(sizeClassIndex);
                        


                    }

                }
                else if (selected.DES)
                {
                    var result = find(treeData.Filenames[i].Normalisations, "Normalisation", "DESEQ");
                    for (var sizeClassIndex = 0; sizeClassIndex < result.SizeClasses.length; sizeClassIndex++)
                    {
                        if (selected.UM)
                        {
                            if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                            {
                                if (selected.COMP)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.Complexity};
                                }
                                else if (selected.NR)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.NonRedundantCount};
                                }
                                else if (selected.R)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Unmapped.RedundantCount};
                                }
                            }
                        }
                        else if (selected.M)
                        {
                            if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                            {
                                if (selected.COMP)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.Complexity};
                                }
                                else if (selected.NR)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.NonRedundantCount};
                                }
                                else if (selected.R)
                                {
                                    sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: result.SizeClasses[sizeClassIndex].Mapped.RedundantCount};
                                }
                            }
                        }
                        else if(selected.All)
                        {
                            if (selected.COMP)
                            {
                                var complexity = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    complexity += result.SizeClasses[sizeClassIndex].Mapped.Complexity;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    complexity += result.SizeClasses[sizeClassIndex].Unmapped.Complexity;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: complexity};
                            }
                            else if (selected.NR)
                            {
                                var NRCount = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    NRCount += result.SizeClasses[sizeClassIndex].Mapped.NonRedundantCount;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    NRCount += result.SizeClasses[sizeClassIndex].Unmapped.NonRedundantCount;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: NRCount};
                            }
                            else if (selected.R)
                            {
                                var RCount = 0;
                                if (typeof result.SizeClasses[sizeClassIndex].Mapped !== "undefined")
                                {
                                    RCount += result.SizeClasses[sizeClassIndex].Mapped.RedundantCount;
                                }
                                if (typeof result.SizeClasses[sizeClassIndex].Unmapped !== "undefined")
                                {
                                    RCount += result.SizeClasses[sizeClassIndex].Unmapped.RedundantCount;
                                }
                                sample[result.SizeClasses[sizeClassIndex].Size] = {x: result.SizeClasses[sizeClassIndex].Size, y: RCount};
                            }
                            
                        }
                        //alert(sizeClassIndex);
                        


                    }

                }



                sampleArray.push(sample);
            }


            var data = [];
            for (var i = 0; i < treeData.Filenames.length; i++)
            {
                var dataObj = {
                    values: sampleArray[i],
                    key: treeData.Filenames[i].Filename,
                    color: wbcolors[i]
                };
                data.push(dataObj);
            }
            console.log(data);
            return data;

        }

//d3.select('#chart1 svg').append("text")
//                .attr("x", 600)
//                .attr("y", 110)
//                .attr("text-anchor", "middle")
//                .text("Sample Charts");
       
    });
}