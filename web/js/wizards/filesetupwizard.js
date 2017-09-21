/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var sampleCount;
var totalSamples = 1;
var repsPerSample;
var samplesetupForm;
var levelsetupForm;
$(function ()
{
    //sample setup form
    samplesetupForm = $("#file_setup_form").show();
    //level setup form
    levelsetupForm = $("#level_setup_form");

    samplesetupForm.steps({
        headerTag: "h3",
        bodyTag: "fieldset",
        transitionEffect: "slideLeft",
        stepsOrientation: "vertical",
        onStepChanging: function (event, currentIndex, newIndex)
        {
            // Allways allow previous action even if the current form is not valid!
            if (currentIndex > newIndex)
            {
                return true;
            }
            if(newIndex === 1)
            {
                sampleCount = Number($("#sampleCount").val());
                repsPerSample = Number($("#replicateCount").val());
                
            }
            // Forbid next action on "Warning" step if the user is to young
            /*if (newIndex === 3 && Number($("#age-2").val()) < 18)
            {
                return false;
            }
            */
            // Needed in some cases if the user went back (clean up)
            if (currentIndex < newIndex)
            {
                // To remove error styles
                samplesetupForm.find(".body:eq(" + newIndex + ") label.error").remove();
                samplesetupForm.find(".body:eq(" + newIndex + ") .error").removeClass("error");
            }
            samplesetupForm.validate().settings.ignore = ":disabled,:hidden";
            return samplesetupForm.valid();
        },
        onStepChanged: function (event, currentIndex, priorIndex)
        {
            if (currentIndex === 1)
            {
                if (sampleCount < totalSamples)
                {
                    for (var i = totalSamples - 1; i > sampleCount; i--)
                    {
                        removeSampleSteps(i);

                    }

                }
                else
                {
                    for (var i = totalSamples - 1; i < sampleCount; i++)
                    {
                        insertSampleSteps(i);
                    }
                }
            }

            /*
            // Used to skip the "Warning" step if the user is old enough.
            if (currentIndex === 2 && Number($("#age-2").val()) >= 18)
            {
                form.steps("next");
            }
            // Used to skip the "Warning" step if the user is old enough and wants to the previous step.
            if (currentIndex === 2 && priorIndex === 3)
            {
                form.steps("previous");
            }*/
        },
        onFinishing: function (event, currentIndex)
        {
            samplesetupForm.validate().settings.ignore = ":disabled";
            return samplesetupForm.valid();
        },
        onFinished: function (event, currentIndex)
        {
            //alert("Submitted!");
            //app.populateFileData(sampleCount, repsPerSample);
            document.getElementById("file_setup_form").style.display="none";
            samplesetupForm.hide();
            document.getElementById("level_setup_form").style.display="block";
            levelsetupForm.show();
        }
    })
    
    levelsetupForm.steps({
        headerTag: "h3",
        bodyTag: "fieldset",
        transitionEffect: "slideLeft",
        stepsOrientation: "vertical",
        onStepChanging: function (event, currentIndex, newIndex)
        {
            // Allways allow previous action even if the current form is not valid!
            if (currentIndex > newIndex)
            {
                return true;
            }
            if(newIndex === 1)
            {
              
                
            }
            // Forbid next action on "Warning" step if the user is to young
            /*if (newIndex === 3 && Number($("#age-2").val()) < 18)
            {
                return false;
            }
            */
            // Needed in some cases if the user went back (clean up)
            if (currentIndex < newIndex)
            {
                // To remove error styles
                levelsetupForm.find(".body:eq(" + newIndex + ") label.error").remove();
                levelsetupForm.find(".body:eq(" + newIndex + ") .error").removeClass("error");
            }
            levelsetupForm.validate().settings.ignore = ":disabled,:hidden";
            return levelsetupForm.valid();
        },
        onStepChanged: function (event, currentIndex, priorIndex)
        {
            

            /*
            // Used to skip the "Warning" step if the user is old enough.
            if (currentIndex === 2 && Number($("#age-2").val()) >= 18)
            {
                form.steps("next");
            }
            // Used to skip the "Warning" step if the user is old enough and wants to the previous step.
            if (currentIndex === 2 && priorIndex === 3)
            {
                form.steps("previous");
            }*/
        },
        onFinishing: function (event, currentIndex)
        {
            levelsetupForm.validate().settings.ignore = ":disabled";
            return levelsetupForm.valid();
        },
        onFinished: function (event, currentIndex)
        {
            //app.populateFileData(sampleCount, repsPerSample);

        }
    })
    
    
    

});
function insertSampleSteps(sampleID)
{
    samplesetupForm.steps("insert",
            totalSamples,
            {
                title: "Sample ID: " + sampleID,
                contentMode: "iframe",
                contentUrl: "../HTML/wizardcontent/samplesetup.html"
            });

    samplesetupForm.steps("previous");
    totalSamples++;
    
    //update the storage values for the sample setup frame
    localStorage.setItem("totalSamples", totalSamples);
    localStorage.setItem("repsPerSample", repsPerSample);
                
}
function removeSampleSteps(sampleID)
{
    samplesetupForm.steps("remove", sampleID);
    totalSamples--;
    
    //update the storage values for the sample setup frame
    localStorage.setItem("totalSamples", totalSamples);
    localStorage.setItem("repsPerSample", repsPerSample);
}