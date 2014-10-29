#######################
# This script allows you to add your local changes to staging, commit them and push them up to git hub in one go.
# It expects a commit message to be supplied when the script is invoked.
# It is advisable to run 'git pull' before running this script and do any merging that is required.
####################### 
if [ -z "$1" ] ; then
  echo "You did not supply a commit message"
  echo Usage: ./gitPush.sh \"Your commit message\"
  exit 1
fi

git add .
git commit -m "$1"
git push
