rm -rf .parcel-cache
rm -rf dist
pnpm parcel build src/index.html --public-url '.'
#git commit . -m "web"
#git push