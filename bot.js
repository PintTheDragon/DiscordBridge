const Discord = require('discord.js');
const client = new Discord.Client();
const uuidv4 = require('uuid/v4');
const WebSocket = require('ws');
var mysql = require('mysql');
var prefix = "|";
var loaded = false;

var con = mysql.createConnection({
  host: "nope",
  user: "nope",
  password: "nope",
  database: "nope"
});
var token = "nope";
var port = 8880;
var helpMsgs = {};
var helpMsg = new Discord.RichEmbed().setTitle("Help").setDescription("*=Admin Command").setColor(0x20998b);
var discServer = {};
var discServerDelete = {};
var baseServer = {"admins": [], "adminChannels": {}, "servers": {}, "players": {}, "prefix": "|"};
var baseServerDelete = {"setTokens": {}, "awaiting": {}, "clients": []};

function isAdmin(msg) {
    if (!msg.member.hasPermission("ADMINISTRATOR") && !discServer[msg.guild.id]["admins"].includes(msg.member.id)) {
        msg.channel.send("You do not have permission to run this command!");
        return false;
    }
    return true;
}

function createHelpMsg(prefixV) {
    var helpMsgEmbed = new Discord.RichEmbed().setTitle("Help").setDescription("*=Admin Command").setColor(0x20998b)
    var cmdObj = [];
    //Commands here
    //Name, command, description
    cmdObj.push(["Help", "help", "Shows every command in this bot."]);
    cmdObj.push(["CP*", "cp <prefix>", "Changes the bot's prefix. This prefix can be anything and of any length."]);
    cmdObj.push(["SetPlayer", "setplayer", "Starts the player linking process."]);
    cmdObj.push(["ResetPlayer", "resetplayer", "Unlinks your discord and minecraft accounts."]);
    cmdObj.push(["AddServer*", "addserver <server name> <authentication string> <channel id for the server>", "Adds a server to the bot (SetupServer is recommened over this)."]);
    cmdObj.push(["DelServer*", "delserver <server name>", "Deletes a server from the bot."]);
    cmdObj.push(["SetupServer*", "setupserver <server name> <#channel>", "The easiest way to add a server to the bot."]);
    cmdObj.push(["SetAdminChannel*", "setadminchannel <server name> <#channel>", "Sets an Admin Channel for a specific server."]);
    cmdObj.push(["DelAdminChannel*", "deladminchannel <server name>", "Deletes an Admin Channel for a specific server."]);
    cmdObj.push(["ACMD*", "acmd <command>", "Sends a console command to the server. Must be used in an admin channel or server channel."]);
    cmdObj.push(["Balance", "bal or " + prefix + "balance", "Returns your current balance. Your player must have been set first."]);
    cmdObj.push(["Spawn", "spawn", "Sends you to spawn. Your player must have been set first."]);
    cmdObj.push(["AddAdmin*", "addadmin <@user>", "Makes a user an admin and lets them use admin commands. You must have the ADMINISTRATOR (discord) permission to use this command."]);
    cmdObj.push(["DelAdmin*", "deladmin <@user>", "Revokes the permission to run admin commands from a user. You must have the ADMINISTRATOR (discord) permission to use this command."]);
	cmdObj.push(["CMD", "cmd <command> <arguments>", "Runs a custom command. These are created by plugins that hook into DiscordBridge's API."]);
    //End commands
    for (var i = 0; i < cmdObj.length; i++) {
        helpMsgEmbed.addField(cmdObj[i][0], prefixV + cmdObj[i][1] + "\n" + cmdObj[i][2], true);
    }
    helpMsgs[prefixV] = helpMsgEmbed;
}

function preload() {
    createHelpMsg();
}

function saveGuild(id){
    con.query("nope");
}

function loadGuilds(){
    con.query("nope", async (err, result) =>{ 
    if(err) throw err;
    
    for(let i = 0; i < result.length; i++){
        loadGuild(nope, nope);
    }
    });
}

function loadGuild(id, data){
    let list = Object.assign({}, baseServer);
    let savedList = JSON.parse(data);
    list["admins"] = savedList["admins"];
    list["adminChannels"] = savedList["adminChannels"];
    list["servers"] = savedList["servers"];
    list["players"] = savedList["players"];
    list["prefix"] = savedList["prefix"];
    createHelpMsg(savedList["prefix"]);
    
    let list2 = Object.assign({}, baseServerDelete);
    discServer[id] = list;
    discServerDelete[id] = list2;
}

function addNewGuild(id){
    con.query("nope");
    loadGuild(id, JSON.stringify(Object.assign({}, baseServer)));
}

//Create server
function createServ() {
const wss = new WebSocket.Server({ port: 8880 });

    wss.on('connection', function connection(socket) {
        try{
        var login = false;
        var name;
        var discID;

        var ip = socket.remoteAddress;

        socket.on('message', function incoming(data) {
            data = data.toString();
            if (data.startsWith("LOGIN ")) {
//LOGIN name auth_str id
                if (data.split(" ").length != 4) {
                    return;
                }
                var t_name = data.split(" ")[1];
                var auth = data.split(" ")[2];
                discID = data.split(" ")[3];
                if(!discServer.hasOwnProperty(discID)){socket.end(); return;}
                if(!discServer[discID]["servers"].hasOwnProperty(t_name)) {socket.end(); return;}
//{name: [ip, auth, channel]}
                if (discServer[discID]["servers"][t_name] != null && discServer[discID]["servers"][t_name][1] == auth) {
                    login = true;
                    name = t_name;
                    discServerDelete[discID]["clients"][name] = socket;
                    socket.send("LCONF");
                    //console.log("Server " + name + " logged in!");
                } else {
                    //console.log("Invalid login from " + ip + ", apparently " + t_name + " (auth token: " + auth + ").");
                    socket.end();

                }
            } else {
                if (login) {
//SEND data here
                    if (data.startsWith("SEND ") && data.split(" ").length > 1) {
                        var mes = "";
                        var arr = data.split(" ");
                        for (var i = 1; i < arr.length; i++) {
                            mes += arr[i] + " ";
                        }
                        if(client.channels.get(discServer[discID]["servers"][name][2]).guild.id == discID)
                        client.channels.get(discServer[discID]["servers"][name][2]).send(mes);
                        return;
                    }

                    if (data.startsWith("SENDA ") && data.split(" ").length > 2) {
                        var mes = "";
                        var arr = data.split(" ");
                        for (var i = 2; i < arr.length; i++) {
                            mes += arr[i] + " ";
                        }
                        if(client.channels.get(data.split(" ")[1]).guild.id == discID)
                        client.channels.get(data.split(" ")[1]).send(mes);
                        return;
                    }
                    if (data.startsWith("SENDAD ") && data.split(" ").length > 1) {
                        var mes = "";
                        var arr = data.split(" ");
                        for (var i = 1; i < arr.length; i++) {
                            mes += arr[i] + " ";
                        }
                        var keys = Object.keys(discServer[discID]["adminChannels"]);
                        if (keys.length < 1) return;
                        for (var y = 0; y < keys.length; y++) {
                            if (discServer[discID]["adminChannels"][keys[y]] == name && client.channels.get(keys[y]).guild.id == discID) {
                                client.channels.get(keys[y]).send(mes);
                            }
                        }

                        return;
                    }
                    if (data.startsWith("SYNC ") && data.split(" ").length == 3) {
                        if (discServerDelete[discID]["awaiting"][discServerDelete[discID]["setTokens"][data.split(" ")[1]]] == null) return;
                        if (discServerDelete[discID]["players"][discServerDelete[discID]["setTokens"][data.split(" ")[1]]] != null) return;
                        delete discServerDelete[discID]["awaiting"][discServerDelete[discID]["setTokens"][data.split(" ")[1]]];
                        let keys = Object.keys(discServer[discID]["players"]);
                        for (var n = 0; n < keys.length; n++) {
                            if (discServer[discID]["players"][keys[n]] == data.split(" ")[2]) {
                                delete discServerDelete[discID]["setTokens"][data.split(" ")[1]];
                                //MeSsaGe Player UUid
                                socket.send(`MSGPUU ${data.split(" ")[2]} This player already has a discord user associated with it. Use /disbunset to de-link it.`);
                                return;
                            }
                        }
                        discServer[discID]["players"][discServerDelete[discID]["setTokens"][data.split(" ")[1]]] = data.split(" ")[2];
                        saveGuild(discID);
                        client.fetchUser(setTokens[data.split(" ")[1]]).then((user) => {
                                user.send("Your player has been set!");
                                socket.send(`MSGPUU ${data.split(" ")[2]} Your player has been set!`);
                            });
                        delete discServerDelete[discID]["setTokens"][data.split(" ")[1]];
                        return;
                    }
                    if (data.startsWith("DESYNC ") && data.split(" ").length == 2) {
                        var uuid = data.split(" ")[1];
                        var keys = discServer[discID]["players"].keys();
                        for (var n = 0; n < keys.length; n++) {
                            if (discServer[discID]["players"][keys[n]] == uuid) {
                                delete discServer[discID]["players"][keys[n]];
                            }
                        }
                    }
                } else {

                }
            }
        });

        socket.on('error', function (ex) {
            console.log(ex);
            socket.end();
            if (login) delete discServerDelete[discID]["clients"][name];
        });

        socket.on('close', function () {
            if (login) {
                delete discServerDelete[discID]["clients"][name];
            }
        });
}
catch(e){
    console.log(e);
}
    });

    console.log("Started server on " + 8880);
}

//Server finished

client.on('ready', async () => {
    console.log("Bot started!");
    createServ();
});

client.on('message', async msg => {
    try{
    if (!loaded) return;
    if (msg.author.bot) return;
    if (msg.content.startsWith(discServer[msg.guild.id]["prefix"] + "help")) {
        if(!helpMsgs.hasOwnProperty(discServer[msg.guild.id]["prefix"])) createHelpMsg(discServer[msg.guild.id]["prefix"]);
        let embedMsg = helpMsgs[discServer[msg.guild.id]["prefix"]];
        msg.channel.send({embed: embedMsg});
        return;
    }
    if (msg.content.startsWith(discServer[msg.guild.id]["prefix"] + "cp")) {
        if (!isAdmin(msg)) return;
        if (msg.content.split(" ").length == 2) {
            discServer[msg.guild.id]["prefix"] = msg.content.split(" ")[1];
            saveGuild(msg.guild.id);
            if(!helpMsgs.hasOwnProperty(discServer[msg.guild.id]["prefix"])) createHelpMsg(discServer[msg.guild.id]["prefix"]);
            return;
        }
        msg.channel.send("Usage: " + discServer[msg.guild.id]["prefix"] + "cp <prefix>");
        return;
    }
    if (msg.content.startsWith(discServer[msg.guild.id]["prefix"] + "setplayer")) {
        if (discServer[msg.guild.id]["players"][String(msg.author.id)] != null) {
            msg.channel.send("You have already set your player. Please use " + discServer[msg.guild.id]["prefix"] + "resetplayer to remove your player from your account!");
            return;
        }
        var uuid = String(uuidv4());
        discServerDelete[msg.guild.id]["awaiting"][msg.author.id] = true;
        discServerDelete[msg.guild.id]["setTokens"][uuid] = String(msg.author.id);
        msg.author.send("Type \"/setplayer " + uuid + "\" (without quotes) ingame to set your player!");
        return;
    }
    if (msg.content.startsWith(discServer[msg.guild.id]["prefix"] + "resetplayer")) {
        if (discServer[msg.guild.id]["players"][String(msg.author.id)] == null) {
            var uuid = String(uuidv4());
            discServerDelete[msg.guild.id]["awaiting"][msg.author.id] = true;
            discServerDelete[msg.guild.id]["setTokens"][uuid] = String(msg.author.id);
            msg.author.send("You must set your player first. You may do this by typing /setplayer "+uuid);
            return;
        }
        delete discServer[msg.guild.id]["players"][String(msg.author.id)];
        saveGuild(msg.guild.id);
        msg.channel.send("Reset your player!");
        return;
    }
    if (msg.content.startsWith(discServer[msg.guild.id]["prefix"] + "addserver")) {
        if (!isAdmin(msg)) return;
//|addserver name ip auth channel
        if (msg.content.split(" ").length != 4) {
            msg.channel.send("Usage: " + discServer[msg.guild.id]["prefix"] + "addserver <name> <ip> <auth string> <channel id>");
            return;
        }
        discServer[msg.guild.id]["servers"][msg.content.split(" ")[1]] = ["", msg.content.split(" ")[2], msg.content.split(" ")[3]];
        saveGuild(msg.guild.id);
            msg.channel.send("Added Server!");
        return;
    }
    if (msg.content.startsWith(discServer[msg.guild.id]["prefix"] + "setupserver")) {
        if (!isAdmin(msg)) return;
//|addserver name ip auth channel
        if (msg.content.split(" ").length != 3) {
            msg.channel.send("Usage: " + discServer[msg.guild.id]["prefix"] + "setupserver <name> <#channel>");
            return;
        }
        var uuid = String(uuidv4());
        if(msg.mentions.channels.length == 0) return;
        discServer[msg.guild.id]["servers"][msg.content.split(" ")[1]] = ["", uuid, msg.mentions.channels.first().id];
        saveGuild(msg.guild.id);
            msg.channel.send("Added Server!");
            let arr = new Buffer(msg.guild.id).toString("base64")+"/"+new Buffer(msg.content.split(" ")[1]).toString("base64")+"/"+ new Buffer(uuid).toString("base64");
            msg.author.send("To finish adding your server, please type this command in the server you want to setup: /disbsetup "+new Buffer(arr).toString('base64'));
        return;
    }
    if (msg.content.startsWith(discServer[msg.guild.id]["prefix"] + "delserver")) {
        if (msg.content.split(" ").length != 2) {
            msg.channel.send("Usage: " + discServer[msg.guild.id]["prefix"] + "delserver <name>");
            return;
        }
        if (discServer[msg.guild.id]["servers"][msg.content.split(" ")[1]] == null) {
            msg.channel.send("You have not set this server. Please use " + discServer[msg.guild.id]["prefix"] + "setupserver <name> <#channel> to add this server!");
            return;
        }
        delete discServer[msg.guild.id]["servers"][msg.content.split(" ")[1]];

        saveGuild(msg.guild.id);
            msg.channel.send("Deleted Server!");
        return;
    }
    if (msg.content == discServer[msg.guild.id]["prefix"] + "spawn" || msg.content.startsWith(discServer[msg.guild.id]["prefix"] + "spawn ")) {
        if (discServer[msg.guild.id]["players"][String(msg.author.id)] == null) {
            var uuid = String(uuidv4());
            discServerDelete[msg.guild.id]["awaiting"][msg.author.id] = true;
            discServerDelete[msg.guild.id]["setTokens"][uuid] = String(msg.author.id);
            msg.author.send("You must set your player first. You may do this by typing /setplayer" + uuid);
            return;
        }

        var keys = Object.keys(discServer[msg.guild.id]["servers"]);
        if (keys.length != 0) {
            for (var i = 0; i < keys.length; i++) {
                if (discServer[msg.guild.id]["servers"][keys[i]][2] == String(msg.channel.id)) {
                    discServerDelete[msg.guild.id]["clients"][keys[i]].send("SPAWN " + discServer[msg.guild.id]["players"][String(msg.author.id)]);
                    return;
                }
            }
        }
        msg.content.send("This command must be used in a Server Channel!");
        return;
    }
    if ((msg.content == discServer[msg.guild.id]["prefix"] + "bal" || msg.content.startsWith(discServer[msg.guild.id]["prefix"] + "bal ")) || (msg.content == discServer[msg.guild.id]["prefix"] + "balance" || msg.content.startsWith(discServer[msg.guild.id]["prefix"] + "balance "))) {
        if (discServer[msg.guild.id]["players"][String(msg.author.id)] == null) {
            var uuid = String(uuidv4());
            discServerDelete[msg.guild.id]["awaiting"][msg.author.id] = true;
            discServerDelete[msg.guild.id]["setTokens"][uuid] = String(msg.author.id);
            msg.author.send("You must set your player first. You may do this by typing /setplayer" + uuid);
            return;
        }

        var keys = Object.keys(discServer[msg.guild.id]["servers"]);
        if (keys.length != 0) {
            for (var i = 0; i < keys.length; i++) {
                if (discServer[msg.guild.id]["servers"][keys[i]][2] == String(msg.channel.id)) {
                    discServerDelete[msg.guild.id]["clients"][keys[i]].send("BAL " + players[String(msg.author.id)]);
                    return;
                }
            }
        }
        msg.content.send("This command must be used in a Server Channel!");
        return;
    }
    if (msg.content.startsWith(discServer[msg.guild.id]["prefix"] + "setadminchannel")) {
        //|setadminchannel server id
        if (!isAdmin(msg)) return;
        if (msg.content.split(" ").length != 3) {
            msg.channel.send("Usage: " + discServer[msg.guild.id]["prefix"] + "setadminchannel <server> <channel id>");
            return;
        }
        discServer[msg.guild.id]["adminChannels"][msg.content.split(" ")[2]] = msg.content.split(" ")[1];
        saveGuild(msg.guild.id);
            msg.channel.send("Admin Channel set!");
        return;
    }
    if (msg.content.startsWith(discServer[msg.guild.id]["prefix"] + "deladminchannel")) {
        //|deladminchannel server
        if (!isAdmin(msg)) return;
        if (msg.content.split(" ").length != 3) {
            msg.channel.send("Usage: " + discServer[msg.guild.id]["prefix"] + "deladminchannel <server>");
            return;
        }
        var keys = Object.keys(discServer[msg.guild.id]["adminChannels"]);
        if (keys.length < 1) return;
        for (var i = 0; i < keys.length; i++) {
            if (discServer[msg.guild.id]["adminChannels"][keys[i]] == msg.content.split(" ")[1]) {
                delete discServer[msg.guild.id]["adminChannels"][keys[i]];
                saveGuild(msg.guild.id);
                    msg.channel.send("Admin Channel deleted!");
                return;
            }
        }
        return;
    }
    if (msg.content.startsWith(discServer[msg.guild.id]["prefix"] + "acmd")) {
        if (!isAdmin(msg)) return;
        if (msg.content.split(" ") < 2) {
            msg.channel.send("Usage: " + discServer[msg.guild.id]["prefix"] + "acmd <command>");
            return;
        }
        var keys = Object.keys(discServer[msg.guild.id]["adminChannels"]);
        if (keys.length != 0) {
            for (var i = 0; i < keys.length; i++) {
                if (keys[i] == String(msg.channel.id)) {
                    discServerDelete[msg.guild.id]["clients"][discServer[msg.guild.id]["adminChannels"][keys[i]]].send("ACMD " + String(msg.channel.id) + " " + msg.content.split(discServer[msg.guild.id]["prefix"] + "acmd ")[1]);
                    return;
                }
            }
        }
        var keys = Object.keys(discServer[msg.guild.id]["servers"]);
        if (keys.length != 0) {
            for (var i = 0; i < keys.length; i++) {
                if (discServer[msg.guild.id]["servers"][keys[i]][2] == String(msg.channel.id)) {
                    discServerDelete[msg.guild.id]["clients"][keys[i]].send("ACMD " + String(msg.channel.id) + " " + msg.content.split(discServer[msg.guild.id]["prefix"] + "acmd ")[0]);
                    return;
                }
            }
        }
        msg.channel.send("This command must be used in an Admin Channel or a Server Channel!");
        return;
    }
    if (msg.content.startsWith(discServer[msg.guild.id]["prefix"] + "addadmin")) {
        if (!msg.member.hasPermission("ADMINISTRATOR")) {
            msg.channel.send("You do not have permission to run this command!");
            return;
        }
        if (msg.content.split(" ") < 2 || msg.mentions.users.array().length == 0) {
            msg.channel.send("Usage: " + discServer[msg.guild.id]["prefix"] + "addadmin <@user>");
            return;
        }
        var user = msg.mentions.users.first().id;
        discServer[msg.guild.id]["admins"].push(user);
        saveGuild(msg.guild.id);
            msg.channel.send("Admin added!");
        return;
    }
    if (msg.content.startsWith(discServer[msg.guild.id]["prefix"] + "deladmin")) {
        if (!msg.member.hasPermission("ADMINISTRATOR")) {
            msg.channel.send("You do not have permission to run this command!");
            return;
        }
        if (msg.content.split(" ") < 2 || msg.mentions.users.array().length == 0) {
            msg.channel.send("Usage: " + discServer[msg.guild.id]["prefix"] + "deladmin <@user>");
            return;
        }
        var user = msg.mentions.users.first().id;
        discServer[msg.guild.id]["admins"] = discServer[msg.guild.id]["admins"].filter(function (e) {
            return e !== user
        });
        saveGuild(msg.guild.id);
            msg.channel.send("Admin removed!");

        return;
    }
	if (msg.content.startsWith(prefix + "cmd")) {
		let uuid = "null";
		if (discServer[msg.guild.id]["players"][String(msg.author.id)] != null) uuid = discServer[msg.guild.id]["players"][String(msg.author.id)];
        var keys = Object.keys(discServer[msg.guild.id]["adminChannels"]);
        if (keys.length != 0) {
            for (var i = 0; i < keys.length; i++) {
                if (keys[i] == String(msg.channel.id)) {
                    discServerDelete[msg.guild.id]["clients"][discServer[msg.guild.id]["adminChannels"][keys[i]]].send("CCMD " + String(msg.channel.id) + " " + String(msg.author.id) + " " + isAdmin(msg) + " " + uuid + " " + msg.content.split(discServer[msg.guild.id]["prefix"] + "cmd ")[1]);
                    return;
                }
            }
        }
        var keys = Object.keys(discServer[msg.guild.id]["servers"]);
        if (keys.length != 0) {
            for (var i = 0; i < keys.length; i++) {
                if (discServer[msg.guild.id]["servers"][keys[i]][2] == String(msg.channel.id)) {
                    discServerDelete[msg.guild.id]["clients"][keys[i]].send("CCMD " + String(msg.channel.id) + " " + String(msg.author.id) + " " + isAdmin(msg) + " " + uuid + " " + msg.content.split(discServer[msg.guild.id]["prefix"] + "cmd ")[1]);
                    return;
                }
            }
        }
        return;
    }
//It's a chat message, send it to the server
    var keys = Object.keys(discServer[msg.guild.id]["servers"]);
    if (keys.length != 0) {
        for (var i = 0; i < keys.length; i++) {
            if (discServer[msg.guild.id]["servers"][keys[i]][2] == String(msg.channel.id)) {
                discServerDelete[msg.guild.id]["clients"][keys[i]].send("CHAT " + msg.author.username + " " + msg.content);
                return;
            }
        }
    }
}
    catch(e){
        console.log(e);
    }
});

client.on("guildCreate", async (guild) => {
    try{
    if(!discServer.hasOwnProperty(guild.id)){
        addNewGuild(guild.id);
    }
    //guild.channels.cache.filter(c => c.type === 'text').find(x => x.position == 0).send("Welcome message.");
    }
    catch(e){
        console.log(e);
    }
});

con.connect(function(err) {
  if (err) throw err;
  preload();
  loadGuilds();
  client.login(token);
  loaded = true;
});