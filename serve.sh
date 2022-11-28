pnpm exec http-server -a localhost -p 8082 --cors &
pnpm exec lcp --proxyUrl  https://www.nekretnine.rs/ &
pnpm parcel src/index.html