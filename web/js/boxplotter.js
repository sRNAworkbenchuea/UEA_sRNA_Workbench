/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


//alert(localStorage.Example);
var selected = {RAW: true, PT: false, UQ: false, TM: false, Q: false, B: false, DES: false};
window.onload = function () {

    buildBP();

};



function buildRAW() {

    if (document.getElementById('raw_radio').checked)
    {
        createRAWBox();
    }
}
;
function updateSelections()
{
    selected.RAW = JSON.parse(localStorage.RAW);
    selected.PT  = JSON.parse(localStorage.PT);
    selected.UQ  = JSON.parse(localStorage.UQ);
    selected.TM  = JSON.parse(localStorage.TM);
    selected.Q   = JSON.parse(localStorage.Q);
    selected.B   = JSON.parse(localStorage.B);
    selected.DES = JSON.parse(localStorage.DES);
    /*
     selected.RAW = document.getElementById('raw_radio').checked;
     selected.PT = document.getElementById('PT_radio').checked;
     selected.UQ = document.getElementById('UQ_radio').checked;
     selected.TM = document.getElementById('TM_radio').checked;
     selected.Q = document.getElementById('Q_radio').checked;
     selected.B = document.getElementById('B_radio').checked;
     */
}
function buildBP() {

    //console.log(selected);
    
    updateSelections();
    loadDataAndPlot();

}
;

function loadDataAndPlot()
{

    treeJSON = d3.json("../json/boxplots.json", function (error, treeData) {

        var categories = [];
        var abundanceData = [];
        var outliers = [];

        var windowID = 1;

        var columnID = 0;
        if (selected.RAW)
        {
            //alert("raw");
  
            var result = find(treeData.LogBases[0].Normalisations, "Normalisation", "NONE");

            for (var filenameIndex = 0; filenameIndex < result.Filenames.length; filenameIndex++)
            {
                
                categories.push(result.Filenames[filenameIndex].Filename + " RAW");
                var abundD = [result.Filenames[filenameIndex].Windows[windowID].Boxplot.Min,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.LQ,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.MED,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.UQ,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.Max];
                abundanceData.push(abundD);
                for (var outlierIndex = 0; outlierIndex < result.Filenames[filenameIndex].Windows[windowID].Boxplot.Outliers.length; outlierIndex++)
                {
                    var outlier = [columnID, result.Filenames[filenameIndex].Windows[windowID].Boxplot.Outliers[outlierIndex]];
                    outliers.push(outlier);
                }
                columnID++;
            }
        }
        if (selected.PT)
        {
            var result = find(treeData.LogBases[0].Normalisations, "Normalisation", "PER_TOTAL");
            for (var filenameIndex = 0; filenameIndex < result.Filenames.length; filenameIndex++)
            {
                categories.push(result.Filenames[filenameIndex].Filename + " PER_TOTAL");

                var abundD = [result.Filenames[filenameIndex].Windows[windowID].Boxplot.Min,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.LQ,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.MED,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.UQ,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.Max];
                abundanceData.push(abundD);
                for (var outlierIndex = 0; outlierIndex < result.Filenames[filenameIndex].Windows[windowID].Boxplot.Outliers.length; outlierIndex++)
                {
                    var outlier = [columnID, result.Filenames[filenameIndex].Windows[windowID].Boxplot.Outliers[outlierIndex]];
                    outliers.push(outlier);
                }
                columnID++;
            }
        }
        if (selected.Q)
        {
            var result = find(treeData.LogBases[0].Normalisations, "Normalisation", "QUANTILE");
            for (var filenameIndex = 0; filenameIndex < result.Filenames.length; filenameIndex++)
            {
                categories.push(result.Filenames[filenameIndex].Filename + " QUANTILE");

                var abundD = [result.Filenames[filenameIndex].Windows[windowID].Boxplot.Min,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.LQ,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.MED,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.UQ,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.Max];
                abundanceData.push(abundD);
                for (var outlierIndex = 0; outlierIndex < result.Filenames[filenameIndex].Windows[windowID].Boxplot.Outliers.length; outlierIndex++)
                {
                    var outlier = [columnID, result.Filenames[filenameIndex].Windows[windowID].Boxplot.Outliers[outlierIndex]];
                    outliers.push(outlier);
                }
                columnID++;
            }
        }
        if (selected.TM)
        {
            var result = find(treeData.LogBases[0].Normalisations, "Normalisation", "TRIMMED_MEAN");
            for (var filenameIndex = 0; filenameIndex < result.Filenames.length; filenameIndex++)
            {
                categories.push(result.Filenames[filenameIndex].Filename + " TRIMMED_MEAN");

                var abundD = [result.Filenames[filenameIndex].Windows[windowID].Boxplot.Min,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.LQ,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.MED,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.UQ,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.Max];
                abundanceData.push(abundD);
                for (var outlierIndex = 0; outlierIndex < result.Filenames[filenameIndex].Windows[windowID].Boxplot.Outliers.length; outlierIndex++)
                {
                    var outlier = [columnID, result.Filenames[filenameIndex].Windows[windowID].Boxplot.Outliers[outlierIndex]];
                    outliers.push(outlier);
                }
                columnID++;
            }

        }
        if (selected.UQ)
        {
            var result = find(treeData.LogBases[0].Normalisations, "Normalisation", "UPPER_QUARTILE");
            for (var filenameIndex = 0; filenameIndex < result.Filenames.length; filenameIndex++)
            {
                categories.push(result.Filenames[filenameIndex].Filename + " UPPER_QUARTILE");

                var abundD = [result.Filenames[filenameIndex].Windows[windowID].Boxplot.Min,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.LQ,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.MED,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.UQ,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.Max];
                abundanceData.push(abundD);
                for (var outlierIndex = 0; outlierIndex < result.Filenames[filenameIndex].Windows[windowID].Boxplot.Outliers.length; outlierIndex++)
                {
                    var outlier = [columnID, result.Filenames[filenameIndex].Windows[windowID].Boxplot.Outliers[outlierIndex]];
                    outliers.push(outlier);
                }
                columnID++;
            }

        }
        if (selected.B)
        {
            var result = find(treeData.LogBases[0].Normalisations, "Normalisation", "BOOTSTRAP");
            for (var filenameIndex = 0; filenameIndex < result.Filenames.length; filenameIndex++)
            {
                categories.push(result.Filenames[filenameIndex].Filename + " BOOTSTRAP");

                var abundD = [result.Filenames[filenameIndex].Windows[windowID].Boxplot.Min,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.LQ,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.MED,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.UQ,
                    result.Filenames[filenameIndex].Windows[windowID].Boxplot.Max];
                abundanceData.push(abundD);
                for (var outlierIndex = 0; outlierIndex < result.Filenames[filenameIndex].Windows[windowID].Boxplot.Outliers.length; outlierIndex++)
                {
                    var outlier = [columnID, result.Filenames[filenameIndex].Windows[windowID].Boxplot.Outliers[outlierIndex]];
                    outliers.push(outlier);
                }
                columnID++;
            }

        }
       
        var chart = new Highcharts.Chart({
            chart: {
                renderTo: 'container',
                type: 'boxplot'
            },
            credits: {
                position: {
                    align: 'left',
                    verticalAlign: 'bottom',
                    x: 10,
                    y: 50
                }
            },
            title: {
                text: 'Abundance Plots'
            },
            legend: {
                enabled: false
            },
            xAxis: {
                categories: categories,
                title: {
                    text: 'Sample Details'
                }
            },
            yAxis: {
                title: {
                    text: 'Abundance'
                }

                },
            plotOptions: {
                series: {
                    fillColor: "#808080",
                    colorByPoint: true,
                    colors: wbcolors
                }
            },
            series: [{
                    name: 'Abundance',
                    color: Highcharts.getOptions().colors[0],
                    data: abundanceData,
                    tooltip: {
                        headerFormat: '<em>Details: {point.key}</em><br/>'
                    }



                }, {
                    name: 'Outlier',
                    color: Highcharts.getOptions().colors[0],
                    type: 'scatter',
                    data: outliers,
                    marker: {
                        fillColor: 'white',
                        lineWidth: 1,
                        lineColor: Highcharts.getOptions().colors[0]
                    },
                    tooltip: {
                        pointFormat: 'Abundance {point.y}'
                    }
                }]
        });

        //console.log(abundanceData);
        /*$(function () {


            // Apply the theme
            $('#container').highcharts({
                chart: {
                    type: 'boxplot'

                },
                credits: {
                    position: {
                        align: 'left',
                        verticalAlign: 'bottom',
                        x: 10,
                        y: 50
                    }
                },
                title: {
                    text: 'Abundance Plots'
                },
                legend: {
                    enabled: false
                },
                xAxis: {
                    categories: categories,
                    title: {
                        text: 'Sample Details'
                    }
                },
                yAxis: {
                    title: {
                        text: 'Abundance'
                    }

                },
                series: [{
                        name: 'Abundance',
                        color: Highcharts.getOptions().colors[0],
                        data: abundanceData,
                        tooltip: {
                            headerFormat: '<em>Details: {point.key}</em><br/>'
                        }



                    }, {
                        name: 'Outlier',
                        color: Highcharts.getOptions().colors[0],
                        type: 'scatter',
                        data: outliers,
                        marker: {
                            fillColor: 'white',
                            lineWidth: 1,
                            lineColor: Highcharts.getOptions().colors[0]
                        },
                        tooltip: {
                            pointFormat: 'Abundance {point.y}'
                        }
                    }]

            });
        });*/

    });


}
