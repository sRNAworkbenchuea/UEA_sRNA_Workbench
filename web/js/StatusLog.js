// author chris
function StatusLog(containerID) {
    
    this.writeLine = function(text)
    {
        var d = new Date();
        var hour = d.getHours() + "";
        var minute = d.getMinutes() + "";
        var second = d.getSeconds() + "";
        while(hour.length < 2)
        {
            hour = "0" + hour;
        }
        while(minute.length < 2)
        {
            minute = "0" + minute;
        }
                while(second.length < 2)
        {
            second = "0" + second;
        }
        var dateStamp = "[" + d.getDate() + "/" + (d.getMonth()+1) + "/" + d.getFullYear() + ":" + hour + ":" + minute + ":" + second + "]";
        document.getElementById(containerID).innerHTML  += dateStamp + text + "<br />";
    };
}