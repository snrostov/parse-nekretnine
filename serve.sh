rm -rf .parcel-cache
rm -rf dist
pnpm exec http-server -a localhost -p 8083 --cors &
#pnpm exec lcp --proxyUrl  https://www.nekretnine.rs/ &
pnpm parcel src/index.html &
read -p "Press enter to continue"
jobs -p
kill $(jobs -p)