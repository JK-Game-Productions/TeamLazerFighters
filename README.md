# TeamLazerFighters
Designed and Developed by **John Wishek** and **Kiana Brunberg**

## Typical game scene
![game_scene](https://github.com/JK-Game-Productions/TeamLazerFighters/assets/31836580/4d4d1226-7fac-40dd-a2ca-52ef32a3b15f)

## How to play
1. To compile and run the single player game, run the **compile.bat** file and the **run.bat** file in order from the game's root file.
2. To use the networked version of the game, navigate to the server directory and run the servers respective compile and run batch files, then in the games root directory after running the clients **compile.bat** file a user must edit the **run.bat** file with the **ip address that the server is hosted on** before running the game to connect to the networked server.
 - *Note: This game runs almost exclusively on mouse and keyboard inputs, controller inputs are not fully supported*
3. When the game first starts the player is presented with the start menu which prompts the player to select a team by pressing one or two on the keyboard. When a team is selected the player is spawned on their respective team’s side facing the opposite team, equipped with a lazer gun and a hud showing their personal score in the bottom left and their teams score at the top of the screen. Firing at another player and successfully hitting them with the lazers increases a player's score and the score of their respective team.

## Player Controls
**W/S Keys** - Moves the player forward and back respectively </br>
**A/D Keys** - Strafes the player left and right respectively </br>
**Left Shift** - Sprint that increases player movement in any direction </br>
**Mouse left and right movement** - turns the player left and right respectively </br>
**Mouse up and down movement** - angles the player view up and down respectively </br>
**Left Mouse Button** - Fires Lazer Gun </br>
**Right Mouse Button** - Aims the Lazer Gun </br>
**Tab Key** - Pause menu/team selection menu </br>
**1 & 2 Keys** - Brings up team selection menu aswell, allows for team switching </br>

## About the Game
- The genre is a FPS game that utilizes a theme in the airsoft/paintball environment with sci-fi weapons and music. The dimensionality of the game is a 3-D, grounded earth like world map from the perspective of the players whose activities are using the terrain and physical ability to out maneuver their opponents and combat them in a way that gives their team a point advantage. </br>
- Input controls are demonstrated in the game with the team selection menu and furthermore in the ability to move and shoot with a keyboard and mouse. Other players may join the game with an operating server. The player and NPC models as well as the player's lazer gun are the required custom models. A single player who starts the game will have a NPC who joins the opposite team to fight.
- At the beginning of the game choosing a team sets the player models color based on the team joined. When a player joins a team a script is run to decide a random location on either side of the map for their team's respective spawn zones. In the distance of the world a skybox of a forest is seen surrounding the game map. The terrain is a river canyon that has near flat planes on either side with slopes leading to the river; players and NPCs follow this terrain and can hide in the valleys.
- Each spawn zone has a light corresponding to the teams coloring the possible locations of player spawns. The river generates a sound the closer a player approaches it, the background synth music and bird sounds play to match the sci-fi elements of the game. A HUD displaying the players score at the lower left and one displaying the teams score is shown at the top. The player model and the lazer gun are a hierarchical system in which no matter where the player is facing the gun is always aimed in front.
- As the player moves there appears a walking and running animation which stops when the player stops moving. NPC’s are to spawn with each player that joins the game, they have AI that is meant to find opponents of the opposite team. The lazer gun produces a lazer that has a velocity and is fired directly from the players facing direction and falls to gravity. When a lazer hits another player or NPC a score is incremented for a player and for the team the player is a part of. </br>


## Scripting Initializations
- Blue/Red Spawn scripting files set a random player location depending on team selection. </br>
- **Game Parameters initialized:** (player)moveSpeed, score, blueScore, redScore, elaps(ed)Time, paused, endGame, viewAxis, isRunning, isWalking, mouseVisible, lazergunAimed, isClientConnected </br>

## Changes/Attrubution
- **Changes Made to Network Protocol:** The ghost npc creation was moved to the ghostManager class. An NPC is created whenever a new player joins the server, this addition was made to the protocol client. When the escape key is hit, the player's ghost avatar is removed before the game shuts down. The isnear functionality was slightly reworked and the server is informed when the avatar is near npc. </br>
- **Changes to the TAGE engine:** The current changes and used functions to the TAGE engine are gyaw(float,float) and pitch(float,float). </br>

- **Kiana’s major contributions** were the lazer gun model, player/NPC animation, networking, and AI. </br>
- **John's major contributions** were the player/NPC model, scripting, terrain, physics and collision detection. </br>
- Items we created were the models of the player/NPC and lazer gun. Textures for red and blue team avatar, lazer beam and crosshair. The terrain height map of the river was also created and implemented by us.

## Assets Used:
- The terrain texture uses a texture found here https://polyhaven.com/a/brown_mud_leaves_01  Attribution to the author here https://www.artstation.com/tuytel to which the texture license is found here https://polyhaven.com/license as CC0 public domain attribution is not required but appreciated. </br>
- The skybox image can be found here https://www.bobgroothuis.com/products/dutch-free-360-hdri-015 Attribution to this website’s owner under the terms of appropriate credit, evidence of the original free license is under creative commons for non-commercial use can be found here https://www.bobgroothuis.com/apps/help-center#hc-new-license-structure-starting-1-august-2019-1 </br>
- The sound file titled birdsInForest can be found here https://pixabay.com/sound-effects/forest-with-small-river-birds-and-nature-field-recording-6735/ Attribution to the username Garuda1982 to which the license permits creative modification and/or without explicit attribution can be found here https://pixabay.com/service/license-summary with a link to their full licenceing terms. </br>
- The sound files titled grassRunning and grassWalking were found here https://www.fesliyanstudios.com/royalty-free-sound-effects-download/footsteps-on-grass-284 Attribution by site owner is requested as “free sound effects from https://www.fesliyanstudios.com” whose policy/license on non-commercial use is free to use with accrediting link can be found https://www.fesliyanstudios.com/policy  </br>
- The laser sound file can be found here https://www.soundfishing.eu/sound/laser-gun as simple laser. Attribution to the web domain Sound Fishing as a sound effect it falls under the LESF license, a royalty free that allows use in both private and commercial projects whose terms can be found here https://www.soundfishing.eu/licence-LESF.html  </br>
- The river sound file can be found here https://pixabay.com/sound-effects/calm-river-ambience-loop-125071/ </br>
