#!/bin/bash

if test "$#" -ne 0; then
    echo "Initial setup of a raspberry pi, ran locally once the git has been downloaded"
    echo "Usage:"
    echo "  $0"
    exit 2
fi

# Check if the script is run as root
if [ "$EUID" -ne 0 ]; then
    echo "Please run this script as root"
    exit
fi

# First we copy the python server
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cp $SCRIPT_DIR/../py/open_dmx_server.py /usr/local/bin/

# Then we prepare the service unit file
cat > /lib/systemd/system/dmx512.service <<- EOM 
[Unit]
 Description=My Sample Service
 After=multi-user.target

[Service]
 Type=idle
 ExecStart=/usr/bin/python /usr/local/bin/open_dmx_server.py

[Install]
 WantedBy=multi-user.target
EOM

# And we can tell systemd to use the new service
systemctl daemon-reload
systemctl enable dmx512.service
