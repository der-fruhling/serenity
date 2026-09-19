(function () {
    const fs = require("node:fs")
    const runnerIndex = config.files.findIndex(function (f) {
        return String(f).indexOf('kotlin-test-karma-runner.js') !== -1;
    });
    const insertAt = runnerIndex === -1 ? 0 : runnerIndex + 1;
    fs.writeFileSync('clear-adapter-transformer-runtime.js', `
if (typeof window !== 'undefined' && window.kotlinTest) {
    delete window.kotlinTest.adapterTransformer;
}
`)
    config.files.splice(insertAt, 0, 'clear-adapter-transformer-runtime.js');
})();
