import { Alert, Platform } from "react-native";


export const showAlert = (title: string, message: string, onConfirm?: () => void) => {
    if (Platform.OS === 'web') {
        window.alert(`${title}: ${message}`);
            if (onConfirm) onConfirm();
    } else {
        Alert.alert(title, message, [
            { text: 'OK', onPress: onConfirm || (() => {}) }
        ]);
    }
};