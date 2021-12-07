function createSlides() {
    return remark.create({
        ratio: '16:9',
        highlightLanguage: 'kotlin',
        highlightStyle: 'monokai',
        navigation: {
            scroll: false
        }
    });
}
