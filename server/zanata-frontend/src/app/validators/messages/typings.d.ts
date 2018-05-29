import ValidationMessages from '../ValidationMessages'
declare module "*.json" {
  const Messages: ValidationMessages;
  export default Messages;
}
