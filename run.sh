#!/usr/bin/env bash

printf '
  /$$$$$$                     /$$                                                         /$$            /$$$$$$
 /$$__  $$                   |__/                                                        | $$           /$$__  $$
| $$  \ $$  /$$$$$$$ /$$$$$$$ /$$  /$$$$$$  /$$$$$$$  /$$$$$$/$$$$   /$$$$$$  /$$$$$$$  /$$$$$$        |__/  \ $$
| $$$$$$$$ /$$_____//$$_____/| $$ /$$__  $$| $$__  $$| $$_  $$_  $$ /$$__  $$| $$__  $$|_  $$_/          /$$$$$$/
| $$__  $$|  $$$$$$|  $$$$$$ | $$| $$  \ $$| $$  \ $$| $$ \ $$ \ $$| $$$$$$$$| $$  \ $$  | $$           /$$____/
| $$  | $$ \____  $$\____  $$| $$| $$  | $$| $$  | $$| $$ | $$ | $$| $$_____/| $$  | $$  | $$ /$$      | $$
| $$  | $$ /$$$$$$$//$$$$$$$/| $$|  $$$$$$$| $$  | $$| $$ | $$ | $$|  $$$$$$$| $$  | $$  |  $$$$/      | $$$$$$$$
|__/  |__/|_______/|_______/ |__/ \____  $$|__/  |__/|__/ |__/ |__/ \_______/|__/  |__/   \___/        |________/
                                  /$$  \ $$
                                 |  $$$$$$/
                                  \______/
'

printf "
   __                      __   __    __                          __
  / /  _______  __ _____ _/ /  / /_  / /____    __ _____  __ __  / /  __ __
 / _ \/ __/ _ \/ // / _  / _ \/ __/ / __/ _ \  / // / _ \/ // / / _ \/ // /
/_.__/_/  \___/\_,_/\_, /_//_/\__/  \__/\___/  \_, /\___/\_,_/ /_.__/\_, /
                   /___/                      /___/                 /___/
"

printf "
██╗  ██╗ █████╗ ██╗   ██╗███████╗    ██╗   ██╗ ██████╗ ██╗   ██╗    ██╗     ██╗   ██╗ ██████╗███████╗███╗   ██╗███████╗    ██╗████████╗██████╗
██║  ██║██╔══██╗██║   ██║██╔════╝    ╚██╗ ██╔╝██╔═══██╗██║   ██║    ██║     ██║   ██║██╔════╝██╔════╝████╗  ██║██╔════╝    ██║╚══██╔══╝╚════██╗
███████║███████║██║   ██║█████╗       ╚████╔╝ ██║   ██║██║   ██║    ██║     ██║   ██║██║     █████╗  ██╔██╗ ██║█████╗      ██║   ██║     ▄███╔╝
██╔══██║██╔══██║╚██╗ ██╔╝██╔══╝        ╚██╔╝  ██║   ██║██║   ██║    ██║     ██║   ██║██║     ██╔══╝  ██║╚██╗██║██╔══╝      ██║   ██║     ▀▀══╝
██║  ██║██║  ██║ ╚████╔╝ ███████╗       ██║   ╚██████╔╝╚██████╔╝    ███████╗╚██████╔╝╚██████╗███████╗██║ ╚████║███████╗    ██║   ██║     ██╗
╚═╝  ╚═╝╚═╝  ╚═╝  ╚═══╝  ╚══════╝       ╚═╝    ╚═════╝  ╚═════╝     ╚══════╝ ╚═════╝  ╚═════╝╚══════╝╚═╝  ╚═══╝╚══════╝    ╚═╝   ╚═╝     ╚═╝

"

# wait for sec to let them appreciate the art work
#sleep 1;
#
printf "Running mvn clean and package\n"
#
mvn clean;
mvn package;
#
sleep 1;

#printf "Removing previously created index and Results\n"
#
rm -rf data/index
rm -rf Results

printf "Executing JAR\n"
java -Xmx2048m -jar querier/target/querier-1.0-SNAPSHOT-shaded.jar

printf "Running trec eval\n"

for entry in "Results"/*
 do
   printf "\n%s \n" "$entry"
   ../trec_eval/trec_eval -m map data/QrelFile "$entry"
   printf "\n"
 done



