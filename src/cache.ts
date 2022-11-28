export const detailsDirHandle = {
    value: null
}

export const cache = new Map<string, Promise<any>>()

export function selectCacheDirectory(set) {
    window.showDirectoryPicker().then(dir => {
        dir.getDirectoryHandle("raw").then(raw => {
            raw.getDirectoryHandle("details", {create: true}).then(details => {
                detailsDirHandle.value = details
                set(true)
            })
        })
    })
}

export function getCached<T>(id, compute: () => Promise<T>): Promise<T> {
    const existed = cache.get(id)
    if (existed) return existed

    if (detailsDirHandle.value) {
        const p = detailsDirHandle.value.getFileHandle(id + ".json")
            .then(dataFileHandle => dataFileHandle.getFile())
            .then(dataFile => dataFile.text())
            .then(text => JSON.parse(text))
            .catch(() => {
                const r = compute()
                r.then(r => putCache(id, r))
                return r
            })

        cache.set(id, p)
        return p
    }

    const dumb = compute()
    cache.set(id, dumb)
    dumb.then(data => putCache(id, data))
    return dumb
}

function putCache(id, value: any) {
    if (detailsDirHandle.value) {
        value.then(resultData => {
            detailsDirHandle.value.getFileHandle(id + ".json", {create: true})
                .then(test => test.createWritable())
                .then(testFile => {
                    testFile.write(JSON.stringify(resultData), undefined, 2)
                    testFile.close()
                })
        })
    }
}